package jetbrains.buildServer.clouds.kubernetes.auth;

import io.fabric8.kubernetes.client.ConfigBuilder;
import jetbrains.buildServer.clouds.kubernetes.KubeCloudException;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnection;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 14.06.17.
 */
public class DefaultServiceAccountAuthStrategy implements KubeAuthStrategy {
    private static final String DEFAULT_SERVICE_ACCOUNT_TOKEN_FILE = "/var/run/secrets/kubernetes.io/serviceaccount/token";

    @NotNull
    @Override
    public String getId() {
        return "service-account";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Default Service Account";
    }

    @Nullable
    @Override
    public String getDescription() {
        return null;
    }

    @NotNull
    @Override
    public ConfigBuilder apply(@NotNull ConfigBuilder clientConfig, @NotNull KubeApiConnection connection) {
        String defaultServiceAccountAuthToken = getDefaultServiceAccountAuthToken();
        if(StringUtil.isEmpty(defaultServiceAccountAuthToken)) throw new KubeCloudException("Can't locate default Kubernetes service account token.");
        return clientConfig.withOauthToken(defaultServiceAccountAuthToken);
    }

    @Nullable
    private String getDefaultServiceAccountAuthToken() {
        try {
            return FileUtil.readText(new File(DEFAULT_SERVICE_ACCOUNT_TOKEN_FILE));
        } catch (IOException e) {
            return null;
        }
    }
}
