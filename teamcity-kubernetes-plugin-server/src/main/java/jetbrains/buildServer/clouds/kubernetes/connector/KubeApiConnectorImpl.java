package jetbrains.buildServer.clouds.kubernetes.connector;

import com.intellij.openapi.diagnostic.Logger;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.client.*;
import java.util.function.Function;
import jetbrains.buildServer.clouds.kubernetes.auth.KubeAuthStrategy;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.StringUtil;
import org.apache.commons.codec.binary.Base64;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 28.05.17.
 */
public class KubeApiConnectorImpl implements KubeApiConnector {
    private static final Logger LOG = Logger.getInstance(KubeApiConnectorImpl.class.getName());

    private static final int DEFAULT_CONNECTION_TIMEOUT_MS = 5 * 1000;
    private static final int DEFAULT_REQUEST_TIMEOUT_MS = 15 * 1000;

    @NotNull
    private final KubeApiConnection myConnectionSettings;
    private KubernetesClient myKubernetesClient;
    private final KubeAuthStrategy myAuthStrategy;

    public KubeApiConnectorImpl(@NotNull KubeApiConnection connectionSettings, @NotNull KubeAuthStrategy authStrategy) {
        myConnectionSettings = connectionSettings;
        myAuthStrategy = authStrategy;
        myKubernetesClient = createClient(createConfig(myConnectionSettings, myAuthStrategy));
    }

    @NotNull
    protected KubernetesClient createClient(@NotNull final Config config){
        return new DefaultKubernetesClient(config);
    }


    protected Config createConfig(@NotNull KubeApiConnection connectionSettings, @NotNull KubeAuthStrategy authStrategy){
        ConfigBuilder configBuilder = new ConfigBuilder()
          .withMasterUrl(connectionSettings.getApiServerUrl())
          .withNamespace(connectionSettings.getNamespace())
          .withRequestTimeout(DEFAULT_REQUEST_TIMEOUT_MS)
          .withConnectionTimeout(DEFAULT_CONNECTION_TIMEOUT_MS);
        final String caCertData = connectionSettings.getCACertData();
        if(StringUtil.isEmptyOrSpaces(caCertData)){
            configBuilder.withTrustCerts(true);
        } else {
            configBuilder.withCaCertData(Base64.encodeBase64String(caCertData.getBytes()));
        }
        configBuilder = authStrategy.apply(configBuilder, connectionSettings);
        return configBuilder.build();
    }

    @NotNull
    @Override
    public KubeApiConnectionCheckResult testConnection() {
        try {
            String currentNamespaceName = myKubernetesClient.getNamespace();
            Namespace currentNamespace = myKubernetesClient.namespaces().withName(currentNamespaceName).get();
            return currentNamespace != null
                    ? KubeApiConnectionCheckResult.ok("Connection successful")
                    : KubeApiConnectionCheckResult.error(
                      String.format("Error connecting to %s: invalid namespace %s", myConnectionSettings.getApiServerUrl(), StringUtil.isEmptyOrSpaces(currentNamespaceName) ? "Default" : currentNamespaceName),
                    false);
        } catch (KubernetesClientException e) {
            boolean needRefresh = e.getStatus().getCode() == 401 && e.getStatus().getMessage().contains("expired");
            return KubeApiConnectionCheckResult.error(String.format("Error connecting to %s: %s", myConnectionSettings.getApiServerUrl(), e.getCause() == null ? e.getMessage() : e.getCause().getMessage()),
                                                      needRefresh);
        } catch (Exception e) {
            return KubeApiConnectionCheckResult.error(
              String.format("Error connecting to %s: %s", myConnectionSettings.getApiServerUrl(), e.getMessage()),
              false
            );
        }
    }

    @NotNull
    @Override
    public Pod createPod(@NotNull Pod podTemplate) {
        return withKubernetesClient(kubernetesClient -> {
            return kubernetesClient.pods().create(podTemplate);
        });
    }

    @Override
    public boolean deletePod(@NotNull Pod pod, long gracePeriod) {
        return withKubernetesClient(kubernetesClient -> {
            return kubernetesClient.pods().withName(pod.getMetadata().getName()).withGracePeriod(0).delete();
        });
    }

    @NotNull
    @Override
    public Collection<Pod> listPods(@NotNull Map<String, String> labels) {
        return withKubernetesClient(kubernetesClient -> {
            return kubernetesClient.pods().withLabels(labels).list().getItems();
        });
    }

    @NotNull
    @Override
    public PodPhase getPodPhase(@NotNull String podName) {
        return withKubernetesClient(kubernetesClient -> {
            final Pod podNow = kubernetesClient.pods().withName(podName).get();
            return podNow == null ? PodPhase.Unknown : PodPhase.valueOf(podNow.getStatus().getPhase());
        });
    }

    @Nullable
    @Override
    public Deployment getDeployment(@NotNull String deploymentName) {
        return withKubernetesClient(kubernetesClient -> {
            return kubernetesClient.extensions().deployments().withName(deploymentName).get();
        });
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
            return CollectionsUtil.convertCollection(kubernetesClient.extensions().deployments().list().getItems(), namespace -> namespace.getMetadata().getName());
        });
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
            LOG.warnAndDebugDetails("An error occurred", kce);
            if (!retrying && kce.getCode()==401){
                if (testConnection().isNeedRefresh()){
                    invalidate();
                    myKubernetesClient = createClient(createConfig(myConnectionSettings, myAuthStrategy));
                }
                return withKubernetesClient(true, function);
            }
            throw kce;
        }
    }
}