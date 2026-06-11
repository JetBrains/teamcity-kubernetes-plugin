
package jetbrains.buildServer.clouds.kubernetes;

import io.fabric8.kubernetes.api.model.HasMetadata;
import jetbrains.buildServer.clouds.CloudErrorInfo;
import jetbrains.buildServer.clouds.CloudInstance;
import jetbrains.buildServer.clouds.InstanceStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 30.05.17.
 */
public interface KubeCloudInstance extends CloudInstance {
    @NotNull
    @Override
    KubeCloudImage getImage();

    void setStatus(final InstanceStatus status);

    /**
     * Updates the instance from the actual state of its backing resource
     * (a Pod for pod-based deploy modes, a custom resource for the custom-resource mode).
     */
    void updateState(HasMetadata resource);

    void setError(@Nullable CloudErrorInfo errorInfo);

    @Nullable
    String getPVCName();
}