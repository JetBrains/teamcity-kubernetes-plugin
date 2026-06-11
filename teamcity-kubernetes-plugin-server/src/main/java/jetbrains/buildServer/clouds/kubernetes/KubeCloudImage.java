
package jetbrains.buildServer.clouds.kubernetes;

import jetbrains.buildServer.clouds.CloudErrorInfo;
import jetbrains.buildServer.clouds.CloudImage;
import jetbrains.buildServer.clouds.kubernetes.connector.CustomResourceContext;
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

    void addStartedInstance(final KubeCloudInstance instance);

    @NotNull
    String getPodSpecMode();

    @Nullable
    String getPodTemplate();

    @Nullable
    String getPVCTemplate();

    /**
     * Raw YAML manifest of the custom resource (e.g. XSmogVM) for the custom-resource deploy mode.
     */
    @Nullable
    String getCustomResourceTemplate();

    boolean isCustomResourceClusterScoped();

    @Nullable
    String getCustomResourcePlural();

    /**
     * Resource type derived from {@link #getCustomResourceTemplate()}, or null when this image
     * does not use the custom-resource deploy mode.
     */
    @Nullable
    CustomResourceContext getCustomResourceContext();

    @Nullable
    String getSourceDeploymentName();

    int getRunningInstanceCount();

    int getInstanceLimit();

    @NotNull
    String getAgentName(@NotNull String instanceName);

    @NotNull
    String getAgentNamePrefix();

    void setErrorInfo(CloudErrorInfo errorInfo);

    boolean isReusingNames();
}