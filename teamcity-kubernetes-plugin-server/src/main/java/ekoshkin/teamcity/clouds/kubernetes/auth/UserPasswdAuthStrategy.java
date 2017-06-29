package ekoshkin.teamcity.clouds.kubernetes.auth;

import ekoshkin.teamcity.clouds.kubernetes.connector.KubeApiConnection;
import io.fabric8.kubernetes.client.ConfigBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static ekoshkin.teamcity.clouds.kubernetes.KubeParametersConstants.PASSWORD;
import static ekoshkin.teamcity.clouds.kubernetes.KubeParametersConstants.USERNAME;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 14.06.17.
 */
public class UserPasswdAuthStrategy implements KubeAuthStrategy {
    public static final String ID = "user-passwd";

    @NotNull
    @Override
    public String getId() {
        return ID;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Username / Password";
    }

    @Nullable
    @Override
    public String getDescription() {
        return null;
    }

    @NotNull
    @Override
    public ConfigBuilder apply(@NotNull ConfigBuilder clientConfig, @NotNull KubeApiConnection connection) {
        String username = connection.getCustomParameter(USERNAME);
        String password = connection.getCustomParameter(PASSWORD);
        return clientConfig.withUsername(username).withPassword(password);
    }
}
