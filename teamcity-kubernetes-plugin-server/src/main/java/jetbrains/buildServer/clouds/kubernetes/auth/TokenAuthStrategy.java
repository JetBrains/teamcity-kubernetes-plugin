package jetbrains.buildServer.clouds.kubernetes.auth;

import io.fabric8.kubernetes.client.ConfigBuilder;
import jetbrains.buildServer.clouds.kubernetes.KubeCloudException;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnection;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants.AUTH_TOKEN;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 26.06.17.
 */
public class TokenAuthStrategy implements KubeAuthStrategy {
    @NotNull
    @Override
    public String getId() {
        return "token";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Token";
    }

    @Nullable
    @Override
    public String getDescription() {
        return "Authenticate with Bearer Token";
    }

    @NotNull
    @Override
    public ConfigBuilder apply(@NotNull ConfigBuilder clientConfig, @NotNull KubeApiConnection connection) {
        String token = connection.getCustomParameter(AUTH_TOKEN);
        if(StringUtil.isEmpty(token)) {
            throw new KubeCloudException("Auth token is empty for connection to " + connection.getApiServerUrl());
        }
        return clientConfig.withOauthToken(token);
    }
}