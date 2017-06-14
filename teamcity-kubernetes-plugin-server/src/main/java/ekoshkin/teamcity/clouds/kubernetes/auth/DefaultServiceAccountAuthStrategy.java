package ekoshkin.teamcity.clouds.kubernetes.auth;

import io.fabric8.kubernetes.client.ConfigBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 14.06.17.
 */
public class DefaultServiceAccountAuthStrategy implements KubeAuthStrategy {
    @NotNull
    @Override
    public String getId() {
        return null;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return null;
    }

    @Nullable
    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void apply(@NotNull ConfigBuilder clientConfig) {

    }
}
