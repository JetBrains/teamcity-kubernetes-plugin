
package jetbrains.buildServer.clouds.kubernetes.podSpec;

import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Pod;
import jetbrains.buildServer.clouds.CloudInstanceUserData;
import jetbrains.buildServer.clouds.kubernetes.KubeCloudImage;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 15.06.17.
 */
public interface BuildAgentPodTemplateProvider {
    @NotNull String getId();
    @NotNull String getDisplayName();
    @Nullable String getDescription();

    @NotNull
    Pod getPodTemplate(@NotNull String instanceName,
                       @NotNull CloudInstanceUserData cloudInstanceUserData,
                       @NotNull KubeCloudImage kubeCloudImage,
                       @NotNull KubeApiConnector apiConnector);

    @Nullable
    PersistentVolumeClaim getPVC(@NotNull String instanceName,
                                 @NotNull KubeCloudImage kubeCloudImage);
}