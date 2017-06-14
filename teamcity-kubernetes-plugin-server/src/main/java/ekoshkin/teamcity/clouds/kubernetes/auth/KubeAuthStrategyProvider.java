package ekoshkin.teamcity.clouds.kubernetes.auth;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 14.06.17.
 */
public interface KubeAuthStrategyProvider {
    @NotNull
    Collection<KubeAuthStrategy> getAll();

    @Nullable
    KubeAuthStrategy find(@Nullable String id);

    @NotNull
    KubeAuthStrategy get(@Nullable String id);
}
