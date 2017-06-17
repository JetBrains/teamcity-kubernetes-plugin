package ekoshkin.teamcity.clouds.kubernetes.podSpec;

import ekoshkin.teamcity.clouds.kubernetes.KubeCloudImage;
import ekoshkin.teamcity.clouds.kubernetes.connector.KubeApiConnection;
import io.fabric8.kubernetes.api.model.Pod;
import jetbrains.buildServer.clouds.CloudInstanceUserData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 15.06.17.
 */
public interface PodTemplateProvider {
    @NotNull String getId();
    @NotNull String getDisplayName();
    @Nullable String getDescription();

    @NotNull
    Pod getPodTemplate(@NotNull CloudInstanceUserData cloudInstanceUserData, @NotNull KubeCloudImage kubeCloudImage, @NotNull KubeApiConnection kubeApiConnection);
}
