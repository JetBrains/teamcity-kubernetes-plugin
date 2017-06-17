package ekoshkin.teamcity.clouds.kubernetes;

import ekoshkin.teamcity.clouds.kubernetes.connector.ImagePullPolicy;
import jetbrains.buildServer.clouds.CloudImage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 28.05.17.
 */
public interface KubeCloudImage extends CloudImage {
    @NotNull
    String getDockerImage();

    @Nullable
    ImagePullPolicy getImagePullPolicy();

    @Nullable
    String getDockerArguments();

    @Nullable
    String getDockerCommand();

    void addInstance(@NotNull KubeCloudInstance instance);

    boolean deleteInstance(@NotNull KubeCloudInstance instance);

    @NotNull
    String getPodSpecMode();

    @Nullable
    String getCustomPodTemplateContent();
}
