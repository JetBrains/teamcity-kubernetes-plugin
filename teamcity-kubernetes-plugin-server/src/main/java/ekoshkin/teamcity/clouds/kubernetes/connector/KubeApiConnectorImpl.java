package ekoshkin.teamcity.clouds.kubernetes.connector;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.jetbrains.annotations.NotNull;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 28.05.17.
 */
public class KubeApiConnectorImpl implements KubeApiConnector {
    @NotNull
    private final KubeApiConnectionSettings myConnectionSettings;
    private KubernetesClient myKubernetesClient;

    public KubeApiConnectorImpl(@NotNull KubeApiConnectionSettings connectionSettings) {
        myConnectionSettings = connectionSettings;
        myKubernetesClient = createClient(myConnectionSettings);
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
        return createClient(myConnectionSettings).pods().delete(pod);
    }

    private static KubernetesClient createClient(KubeApiConnectionSettings connectionSettings)  {
        ConfigBuilder builder = new ConfigBuilder()
                .withMasterUrl(connectionSettings.getApiServerUrl())
                .withUsername(connectionSettings.getUsername())
                .withPassword(connectionSettings.getPassword())
                .withNamespace(connectionSettings.getNamespace());
        return new DefaultKubernetesClient(builder.build());
    }
}
