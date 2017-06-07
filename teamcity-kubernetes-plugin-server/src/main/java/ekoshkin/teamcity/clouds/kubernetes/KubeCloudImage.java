package ekoshkin.teamcity.clouds.kubernetes;

import jetbrains.buildServer.clouds.CloudImage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 28.05.17.
 */
public interface KubeCloudImage extends CloudImage {
    @NotNull
    String getContainerImage();

    boolean isAlwaysPullImage();

    @Nullable
    String getContainerArguments();

    @Nullable
    String getContainerCommand();
}
