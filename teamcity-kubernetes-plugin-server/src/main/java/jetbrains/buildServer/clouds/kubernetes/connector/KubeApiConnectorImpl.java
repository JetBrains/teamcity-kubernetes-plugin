
package jetbrains.buildServer.clouds.kubernetes.connector;

import com.intellij.openapi.diagnostic.Logger;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import jetbrains.buildServer.clouds.CloudException;
import jetbrains.buildServer.clouds.kubernetes.auth.KubeAuthStrategy;
import jetbrains.buildServer.clouds.kubernetes.connection.KubernetesCredentialsFactory;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 28.05.17.
 */
public class KubeApiConnectorImpl implements KubeApiConnector {
    private static final Logger LOG = Logger.getInstance(KubeApiConnectorImpl.class.getName());

    @NotNull private final String myProjectId;
    @NotNull private final String myProfileId;
    @NotNull private final KubeApiConnection myConnectionSettings;
    @NotNull private final KubeAuthStrategy myAuthStrategy;
    @NotNull private final KubernetesCredentialsFactory myCredentialsFactory;

    private volatile Config myConfig;
    private volatile KubernetesClient myKubernetesClient;
    private volatile boolean myCloseInitiated = false;

    public KubeApiConnectorImpl(@NotNull String projectId, @NotNull String profileId, @NotNull KubeApiConnection connectionSettings, @NotNull KubeAuthStrategy authStrategy, @NotNull
                                KubernetesCredentialsFactory kubernetesCredentialsFactory) {
        myProjectId = projectId;
        myProfileId = profileId;
        myConnectionSettings = connectionSettings;
        myAuthStrategy = authStrategy;
        myCredentialsFactory = kubernetesCredentialsFactory;
        myConfig = myCredentialsFactory.createConfig(myConnectionSettings, myAuthStrategy, myProjectId, myProfileId);
        myKubernetesClient = createClient(myConfig);
    }

    @NotNull
    protected KubernetesClient createClient(@NotNull final Config config){
        if (myCloseInitiated){
            throw new CloudException("Attempting to create a KubernetesClient for a closing api connector");
        }
        LOG.info("Creating new client with config" + getConfigDescription(config));
        return new DefaultKubernetesClient(config);
    }

    @NotNull
    @Override
    public KubeApiConnectionCheckResult testConnection() {
        try {
            String currentNamespaceName = myConfig.getNamespace();
            Namespace currentNamespace = myKubernetesClient.namespaces().withName(currentNamespaceName).get();
            return currentNamespace != null
                    ? KubeApiConnectionCheckResult.ok("Connection successful")
                    : KubeApiConnectionCheckResult.error(
                      String.format("Error connecting to %s: invalid namespace %s", myConfig.getMasterUrl(), StringUtil.isEmptyOrSpaces(currentNamespaceName) ? "Default" : currentNamespaceName),
                    false);
        } catch (KubernetesClientException e) {
            return KubeApiConnectionCheckResult.error(String.format("Error connecting to %s: %s", myConfig.getMasterUrl(), e.getCause() == null ? e.getMessage() : e.getCause().getMessage()),
                                                      e.getStatus() != null && e.getStatus().getCode() == 401);
        } catch (Exception e) {
            return KubeApiConnectionCheckResult.error(
              String.format("Error connecting to %s: %s", myConfig.getMasterUrl(), e.getMessage()),
              false
            );
        }
    }

    @NotNull
    @Override
    public Pod createPod(@NotNull Pod podTemplate) {
        return withKubernetesClient(kubernetesClient -> kubernetesClient.pods().create(podTemplate));
    }

    @Override
    public PersistentVolumeClaim createPVC(@NotNull final PersistentVolumeClaim pvc) {
        return withKubernetesClient(kubernetesClient -> kubernetesClient.persistentVolumeClaims().create(pvc));
    }

    @Override
    public boolean deletePod(@NotNull String podName, long gracePeriod) {
        return withKubernetesClient(kubernetesClient -> kubernetesClient.pods().withName(podName).delete());
    }

    @NotNull
    @Override
    public Collection<Pod> listPods(@NotNull Map<String, String> labels) {
        return withKubernetesClient(kubernetesClient -> kubernetesClient.pods().withLabels(labels).list().getItems());
    }

    @Nullable
    @Override
    public Deployment getDeployment(@NotNull String deploymentName) {
        return withKubernetesClient(kubernetesClient -> kubernetesClient.apps().deployments().withName(deploymentName).get());
    }

    @Override
    public String getNamespace() {
        return myConfig.getNamespace();
    }

    @Nullable
    @Override
    public PodStatus getPodStatus(@NotNull String podName) {
        return withKubernetesClient(kubernetesClient -> {
            final Pod podNow = kubernetesClient.pods().withName(podName).get();
            return podNow == null ? null : podNow.getStatus();
        });
    }

    @NotNull
    @Override
    public Collection<String> listNamespaces() {
        return withKubernetesClient(kubernetesClient -> {
            return CollectionsUtil.convertCollection(kubernetesClient.namespaces().list().getItems(), namespace -> namespace.getMetadata().getName());
        });
    }

    @NotNull
    @Override
    public Collection<String> listDeployments() {
        return withKubernetesClient(kubernetesClient -> {
            return CollectionsUtil.convertCollection(kubernetesClient.apps().deployments().list().getItems(), namespace -> namespace.getMetadata().getName());
        });
    }

    public boolean deletePVC(@NotNull String name){
        return withKubernetesClient(kubernetesClient -> kubernetesClient.persistentVolumeClaims().withName(name).delete());
    }

    @Override
    public void invalidate() {
        myAuthStrategy.invalidate(myConnectionSettings);
    }

    private <T> T withKubernetesClient(Function<KubernetesClient, T> function){
        return withKubernetesClient(false, function);
    }

    private <T> T withKubernetesClient(boolean retrying, Function<KubernetesClient, T> function){
        try {
            return function.apply(myKubernetesClient);
        } catch (KubernetesClientException kce){
            final String operation = getOperation(kce);
            LOG.warnAndDebugDetails(String.format("An error occurred at %s, ProfileId: %s, Code: %d, Status: %s", operation, myProfileId, kce.getCode(), kce.getStatus()),
                                    KubernetesClientException.launderThrowable(kce));
            if (!retrying && kce.getCode()==401){
                final KubeApiConnectionCheckResult result = testConnection();
                LOG.info(String.format("Test connection for %s: %s", myProfileId, result));
                if (result.isNeedRefresh()){
                    LOG.info("Will now invalidate and recreate client for " + myProfileId);
                    invalidate();
                    KubernetesClient oldClient = myKubernetesClient;
                    myConfig = myCredentialsFactory.createConfig(myConnectionSettings, myAuthStrategy, myProjectId, myProfileId);
                    myKubernetesClient = createClient(myConfig);
                    FileUtil.close(oldClient);
                }
                return withKubernetesClient(true, function);
            }
            throw kce;
        } catch (IllegalArgumentException illegalArgumentException){
          LOG.warnAndDebugDetails(String.format("Failed to access the Kubernetes cluster: credentials are invalid or lack required permissions for %s", myProfileId),
                                  illegalArgumentException);
          throw new CloudException("Failed to access the Kubernetes cluster: credentials are invalid or lack required permissions. Please check server logs for more information", illegalArgumentException);
        }
    }

    @Nullable
    private String getOperation(@NotNull KubernetesClientException kce){
        final StackTraceElement[] stackTrace = kce.getStackTrace();
        for (int i=0; i<stackTrace.length; i++){
            if (stackTrace[i].getClassName().contains("jetbrains.buildServer")){
                return stackTrace[i].toString();
            }
        }
        return null;
    }

    private static String getConfigDescription(Config config){
        if (config.getOauthToken() != null) {
            String token = config.getOauthToken();
            String tokenMask;
            if (token.length() <= 16) {
                tokenMask = "Updated on  " + new Date();
            } else {
                tokenMask = String.format("%s...%s", token.substring(0, 4), token.substring(token.length() - 4));
            }
            return " with OAuthToken: " + tokenMask;
        }
        else
            return "";
    }

    @Override
    public void close() {
        myCloseInitiated = true;
        if (myKubernetesClient != null){
            myKubernetesClient.close();
        }
    }
}