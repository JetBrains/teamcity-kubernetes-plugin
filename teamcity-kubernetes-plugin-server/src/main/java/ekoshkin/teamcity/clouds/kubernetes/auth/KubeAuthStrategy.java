package ekoshkin.teamcity.clouds.kubernetes.auth;

import io.fabric8.kubernetes.client.ConfigBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 14.06.17.
 */
public interface KubeAuthStrategy {
    @NotNull String getId();
    @NotNull String getDisplayName();
    @Nullable String getDescription();
    void apply(@NotNull ConfigBuilder clientConfig);
}
