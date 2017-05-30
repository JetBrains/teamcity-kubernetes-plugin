package ekoshkin.teamcity.clouds.kubernetes;

import jetbrains.buildServer.clouds.CloudImage;
import org.jetbrains.annotations.NotNull;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 28.05.17.
 */
public interface KubeCloudImage extends CloudImage {
    @NotNull
    String getDeploymentName();

    @NotNull
    String getImage();
}
