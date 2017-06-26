package ekoshkin.teamcity.clouds.kubernetes.auth;

import ekoshkin.teamcity.clouds.kubernetes.connector.KubeApiConnection;
import io.fabric8.kubernetes.client.ConfigBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 26.06.17.
 */
public class ClientCertificateAuthStrategy implements KubeAuthStrategy {
    @NotNull
    @Override
    public String getId() {
        return "client-cert";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Client Certificate";
    }

    @Nullable
    @Override
    public String getDescription() {
        return "Authenticate with X509 Client Certificate";
    }

    @NotNull
    @Override
    public ConfigBuilder apply(@NotNull ConfigBuilder clientConfig, @NotNull KubeApiConnection connection) {
        //TODO: implement
        return null;
    }
}
