package jetbrains.buildServer.clouds.kubernetes.auth;

import io.fabric8.kubernetes.client.ConfigBuilder;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 14.06.17.
 */
public interface KubeAuthStrategy {

    String SECURE_PREFIX = "secure:";

    @NotNull String getId();

    @NotNull String getDisplayName();

    @Nullable String getDescription();

    @NotNull ConfigBuilder apply(@NotNull ConfigBuilder clientConfig, @NotNull KubeApiConnection connection);

    default boolean isRefreshable(){
        return false;
    }

    default void invalidate(@NotNull final KubeApiConnection connection){

    }
}
