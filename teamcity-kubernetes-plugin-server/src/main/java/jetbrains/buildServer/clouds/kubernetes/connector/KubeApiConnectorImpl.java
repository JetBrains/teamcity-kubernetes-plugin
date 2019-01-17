package jetbrains.buildServer.clouds.kubernetes.connector;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.client.*;
import jetbrains.buildServer.clouds.kubernetes.KubeCloudException;
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
    private static final int DEFAULT_CONNECTION_TIMEOUT_MS = 5 * 1000;
    private static final int DEFAULT_REQUEST_TIMEOUT_MS = 15 * 1000;

    @NotNull
    private final KubeApiConnection myConnectionSettings;
    private final KubernetesClient myKubernetesClient;
    private final KubeAuthStrategy myAuthStrategy;

    private KubeApiConnectorImpl(@NotNull KubeApiConnection connectionSettings, @NotNull Config config, @NotNull KubeAuthStrategy authStrategy) {
        myConnectionSettings = connectionSettings;
        myKubernetesClient = new DefaultKubernetesClient(config);
        myAuthStrategy = authStrategy;
    }

    @NotNull
    public static KubeApiConnectorImpl create(@NotNull KubeApiConnection connectionSettings, @NotNull KubeAuthStrategy authStrategy) throws KubeCloudException {
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
        if ("user-passwd".equals(connectionSettings.getCustomParameter("authStrategy")) &&
          StringUtil.isNotEmpty(connectionSettings.getCustomParameter("username"))){
            configBuilder
              .withUsername(connectionSettings.getCustomParameter("username"))
              .withPassword(connectionSettings.getCustomParameter("secure:password"));
        }
        if ("token".equals(connectionSettings.getCustomParameter("authStrategy")) &&
          StringUtil.isNotEmpty(connectionSettings.getCustomParameter("secure:authToken"))){
            configBuilder.withOauthToken(connectionSettings.getCustomParameter("secure:authToken"));
        }
        return new KubeApiConnectorImpl(connectionSettings, configBuilder.build(), authStrategy);
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
        return myKubernetesClient.pods().create(podTemplate);
    }

    @Override
    public boolean deletePod(@NotNull Pod pod, long gracePeriod) {
        return myKubernetesClient.pods().withName(pod.getMetadata().getName()).withGracePeriod(0).delete();
    }

    @NotNull
    @Override
    public Collection<Pod> listPods(@NotNull Map<String, String> labels) {
        return myKubernetesClient.pods().withLabels(labels).list().getItems();
    }

    @NotNull
    @Override
    public PodPhase getPodPhase(@NotNull String podName) {
        final Pod podNow = myKubernetesClient.pods().withName(podName).get();
        return podNow == null ? PodPhase.Unknown : PodPhase.valueOf(podNow.getStatus().getPhase());
    }

    @Nullable
    @Override
    public Deployment getDeployment(@NotNull String deploymentName) {
        return myKubernetesClient.extensions().deployments().withName(deploymentName).get();
    }

    @Nullable
    @Override
    public PodStatus getPodStatus(@NotNull String podName) {
        final Pod podNow = myKubernetesClient.pods().withName(podName).get();
        return podNow == null ? null : podNow.getStatus();
    }

    @NotNull
    @Override
    public Collection<String> listNamespaces() {
        return CollectionsUtil.convertCollection(myKubernetesClient.namespaces().list().getItems(), namespace -> namespace.getMetadata().getName());
    }

    @NotNull
    @Override
    public Collection<String> listDeployments() {
        return CollectionsUtil.convertCollection(myKubernetesClient.extensions().deployments().list().getItems(), namespace -> namespace.getMetadata().getName());
    }

    @Override
    public void invalidate() {
        myAuthStrategy.invalidate(myConnectionSettings);
    }
}