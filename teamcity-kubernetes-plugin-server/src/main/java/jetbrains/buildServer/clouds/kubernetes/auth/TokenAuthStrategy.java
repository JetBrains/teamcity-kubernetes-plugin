
package jetbrains.buildServer.clouds.kubernetes.auth;

import io.fabric8.kubernetes.client.ConfigBuilder;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import jetbrains.buildServer.clouds.kubernetes.KubeCloudException;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnection;
import jetbrains.buildServer.serverSide.InvalidProperty;
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
        return "Authenticate with a bearer token";
    }

    @NotNull
    @Override
    public ConfigBuilder apply(@NotNull ConfigBuilder clientConfig, @NotNull KubeApiConnection connection) {
        String token = connection.getCustomParameter(SECURE_PREFIX + AUTH_TOKEN);
        if(StringUtil.isEmpty(token)) {
            throw new KubeCloudException("Auth token is empty for connection to " + connection.getApiServerUrl());
        }
        return clientConfig.withOauthToken(token);
    }

    @Override
    public Collection<InvalidProperty> process(final Map<String, String> properties) {
        if (StringUtil.isEmpty(properties.get(SECURE_PREFIX+AUTH_TOKEN))){
            return Collections.singletonList(new InvalidProperty( AUTH_TOKEN, "Token is required"));
        }
        return Collections.emptyList();
    }
}