
package jetbrains.buildServer.clouds.kubernetes.auth;

import io.fabric8.kubernetes.client.ConfigBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnection;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants.*;

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
        String password = connection.getCustomParameter(SECURE_PREFIX+PASSWORD);
        return clientConfig.withUsername(username).withPassword(password);
    }

    @Override
    public Collection<InvalidProperty> process(final Map<String, String> properties) {
        Collection<InvalidProperty> retval = new ArrayList<>();
        if (StringUtil.isEmpty(properties.get(USERNAME))){
            retval.add(new InvalidProperty(USERNAME, "Username is required"));
        }
        if (StringUtil.isEmpty(properties.get(SECURE_PREFIX+ PASSWORD))){
            retval.add(new InvalidProperty(PASSWORD, "Username is required"));
        }
        return retval;
    }
}