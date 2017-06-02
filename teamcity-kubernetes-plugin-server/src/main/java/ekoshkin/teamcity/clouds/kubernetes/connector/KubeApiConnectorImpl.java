package ekoshkin.teamcity.clouds.kubernetes.connector;

import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 28.05.17.
 */
public class KubeApiConnectorImpl implements KubeApiConnector {
    @NotNull
    @Override
    public Deployment createDeployment(String deploymentName, String image) throws KubeApiException {
        return null;
    }

    @NotNull
    @Override
    public Deployment patchDeployment(String deploymentName, KubeObjectPatch patch) throws KubeApiException {
        return null;
    }

    @Nullable
    @Override
    public Deployment findDeployment(@NotNull String name) {
        return null;
    }

    @Override
    public boolean deleteDeployment(String name) {
        return false;
    }

    @Override
    public boolean deletePod(String name) {
        return false;
    }

    @NotNull
    @Override
    public KubeApiConnectionCheckResult testConnection(@NotNull KubeApiConnectionSettings connectionSettings) {
        try {
            KubernetesClient client = createClient(connectionSettings);
            client.pods().list();
            return KubeApiConnectionCheckResult.ok("Connection successful");
        } catch (KubernetesClientException e) {
            return KubeApiConnectionCheckResult.error(String.format("Error connecting to %s: %s", connectionSettings.getApiServerUrl(),
                    e.getCause() == null ? e.getMessage() : e.getCause().getMessage()));
        } catch (Exception e) {
            return KubeApiConnectionCheckResult.error(String.format("Error connecting to %s: %s", connectionSettings.getApiServerUrl(), e.getMessage()));
        }
    }

    private KubernetesClient createClient(KubeApiConnectionSettings connectionSettings)  {
        ConfigBuilder builder = new ConfigBuilder()
                .withMasterUrl(connectionSettings.getApiServerUrl())
                .withUsername(connectionSettings.getAccountName())
                .withOauthToken(connectionSettings.getAccountToken());
        return new DefaultKubernetesClient(builder.build());
    }
}
