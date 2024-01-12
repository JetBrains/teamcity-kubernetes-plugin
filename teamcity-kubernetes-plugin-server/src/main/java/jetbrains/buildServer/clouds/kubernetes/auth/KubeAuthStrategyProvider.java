
package jetbrains.buildServer.clouds.kubernetes.auth;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 14.06.17.
 */
public interface KubeAuthStrategyProvider {
    @Nullable
    KubeAuthStrategy find(@Nullable String id);

    @NotNull
    KubeAuthStrategy get(@Nullable String id);
}