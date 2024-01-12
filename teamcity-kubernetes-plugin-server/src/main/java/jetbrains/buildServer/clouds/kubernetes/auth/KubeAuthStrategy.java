
package jetbrains.buildServer.clouds.kubernetes.auth;

import io.fabric8.kubernetes.client.ConfigBuilder;
import java.util.Map;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnection;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 14.06.17.
 */
public interface KubeAuthStrategy extends PropertiesProcessor {

    String SECURE_PREFIX = "secure:";

    @NotNull String getId();

    @NotNull String getDisplayName();

    @Nullable String getDescription();

    @NotNull ConfigBuilder apply(@NotNull ConfigBuilder clientConfig, @NotNull KubeApiConnection connection);

    default boolean isRefreshable(){
        return false;
    }

    default boolean requiresServerUrl() {
        return true;
    }

    default void invalidate(@NotNull final KubeApiConnection connection){}

    default boolean isAvailable(@Nullable String projectId){
        return true;
    }

    default void fillAdditionalSettings(@NotNull Map<String, Object> mv, boolean isAvailable){}
}