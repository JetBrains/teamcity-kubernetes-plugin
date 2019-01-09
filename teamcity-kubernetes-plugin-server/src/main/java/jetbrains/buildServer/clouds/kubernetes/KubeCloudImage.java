package jetbrains.buildServer.clouds.kubernetes;

import jetbrains.buildServer.clouds.CloudImage;
import jetbrains.buildServer.clouds.CloudInstance;
import jetbrains.buildServer.clouds.CloudInstanceUserData;
import jetbrains.buildServer.clouds.kubernetes.connector.ImagePullPolicy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 28.05.17.
 */
public interface KubeCloudImage extends CloudImage {
    @Nullable
    String getDockerImage();

    @Nullable
    ImagePullPolicy getImagePullPolicy();

    @Nullable
    String getDockerArguments();

    @Nullable
    String getDockerCommand();

    void populateInstances();

    @NotNull
    String getPodSpecMode();

    @Nullable
    String getCustomPodTemplateSpec();

    @Nullable
    String getSourceDeploymentName();

    int getRunningInstanceCount();

    int getInstanceLimit();

    @NotNull
    String getAgentName(@NotNull String instanceName);

    @NotNull
    String getAgentNamePrefix();

    @NotNull
    CloudInstance startNewInstance(@NotNull CloudInstanceUserData instanceUserData, @NotNull KubeCloudClientParametersImpl clientParams);
}
