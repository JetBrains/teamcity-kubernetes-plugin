package ekoshkin.teamcity.clouds.kubernetes.connector;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 01.06.17.
 */
public interface KubeApiConnection {
    @NotNull
    String getApiServerUrl();

    @Nullable
    String getNamespace();

    @Nullable
    String getCustomParameter(@NotNull String parameterName);
}
