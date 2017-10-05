package jetbrains.buildServer.clouds.kubernetes.connector;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.client.*;
import jetbrains.buildServer.clouds.kubernetes.KubeCloudException;
import jetbrains.buildServer.clouds.kubernetes.auth.KubeAuthStrategy;
import jetbrains.buildServer.util.CollectionsUtil;
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
    private KubernetesClient myKubernetesClient;

    public KubeApiConnectorImpl(@NotNull KubeApiConnection connectionSettings, @NotNull Config config) {
        myConnectionSettings = connectionSettings;
        myKubernetesClient = new DefaultKubernetesClient(config);
    }

    @NotNull
    public static KubeApiConnectorImpl create(@NotNull KubeApiConnection connectionSettings, @NotNull KubeAuthStrategy authStrategy) throws KubeCloudException {
        ConfigBuilder configBuilder = new ConfigBuilder()
                .withMasterUrl(connectionSettings.getApiServerUrl())
                .withNamespace(connectionSettings.getNamespace())
                .withRequestTimeout(DEFAULT_REQUEST_TIMEOUT_MS)
                .withConnectionTimeout(DEFAULT_CONNECTION_TIMEOUT_MS);
        configBuilder = authStrategy.apply(configBuilder, connectionSettings);
        return new KubeApiConnectorImpl(connectionSettings, configBuilder.build());
    }

    @NotNull
    @Override
    public KubeApiConnectionCheckResult testConnection() {
        try {
            myKubernetesClient.pods().list();
            return KubeApiConnectionCheckResult.ok("Connection successful");
        } catch (KubernetesClientException e) {
            return KubeApiConnectionCheckResult.error(String.format("Error connecting to %s: %s", myConnectionSettings.getApiServerUrl(),
                    e.getCause() == null ? e.getMessage() : e.getCause().getMessage()));
        } catch (Exception e) {
            return KubeApiConnectionCheckResult.error(String.format("Error connecting to %s: %s", myConnectionSettings.getApiServerUrl(), e.getMessage()));
        }
    }

    @NotNull
    @Override
    public Pod createPod(@NotNull Pod podTemplate) {
        return myKubernetesClient.pods().create(podTemplate);
    }

    @Override
    public boolean deletePod(@NotNull Pod pod) {
        return myKubernetesClient.pods().delete(pod);
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
}
