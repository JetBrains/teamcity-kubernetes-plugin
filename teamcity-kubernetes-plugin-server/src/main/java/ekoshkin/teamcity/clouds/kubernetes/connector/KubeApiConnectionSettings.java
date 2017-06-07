package ekoshkin.teamcity.clouds.kubernetes.connector;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 01.06.17.
 */
public interface KubeApiConnectionSettings {
    @NotNull
    String getApiServerUrl();

    @NotNull
    String getPassword();

    @NotNull
    String getUsername();

    @Nullable
    String getNamespace();
}
