package ekoshkin.teamcity.clouds.kubernetes.connector;

import ekoshkin.teamcity.clouds.kubernetes.auth.KubeAuthStrategy;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 28.05.17.
 */
public class KubeApiConnectorImpl implements KubeApiConnector {
    @NotNull
    private final KubeApiConnection myConnectionSettings;
    private KubernetesClient myKubernetesClient;

    public KubeApiConnectorImpl(@NotNull KubeApiConnection connectionSettings, @NotNull KubeAuthStrategy authStrategy) {
        myConnectionSettings = connectionSettings;

        ConfigBuilder configBuilder = new ConfigBuilder()
                .withMasterUrl(connectionSettings.getApiServerUrl())
                .withNamespace(connectionSettings.getNamespace());
        configBuilder = authStrategy.apply(configBuilder, connectionSettings);

        myKubernetesClient = new DefaultKubernetesClient(configBuilder.build());
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
    public Collection<Pod> listPods(Map<String, String> labels) {
        return myKubernetesClient.pods().withLabels(labels).list().getItems();
    }

    @NotNull
    @Override
    public PodPhase getPodPhase(@NotNull Pod pod) {
        final Pod podNow = myKubernetesClient.pods().withName(pod.getMetadata().getName()).get();
        return podNow == null ? PodPhase.Unknown : PodPhase.valueOf(podNow.getStatus().getPhase());
    }

    @Nullable
    @Override
    public Deployment getDeployment(@NotNull String deploymentName) {
        return myKubernetesClient.extensions().deployments().withName(deploymentName).get();
    }
}
