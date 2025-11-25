
package jetbrains.buildServer.clouds.kubernetes.podSpec;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import jetbrains.buildServer.clouds.kubernetes.KubeCloudClientParameters;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 04.09.17.
 */
public interface DeploymentContentProvider {
    @Nullable
    Deployment findDeployment(@NotNull String projectId, @NotNull String profileId, @NotNull String name, @NotNull KubeCloudClientParameters kubeClientParams);
}