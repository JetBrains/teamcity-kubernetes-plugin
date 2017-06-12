package ekoshkin.teamcity.clouds.kubernetes;

import ekoshkin.teamcity.clouds.kubernetes.connector.ImagePullPolicy;
import ekoshkin.teamcity.clouds.kubernetes.connector.KubeApiConnector;
import jetbrains.buildServer.clouds.CloudErrorInfo;
import jetbrains.buildServer.clouds.CloudInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 07.06.17.
 */
public class KubeCloudImageImpl implements KubeCloudImage {
    private final KubeApiConnector myApiConnector;
    private final KubeCloudImageData myImageData;

    public KubeCloudImageImpl(@NotNull final KubeCloudImageData kubeCloudImageData, @NotNull final KubeApiConnector apiConnector) {
        myImageData = kubeCloudImageData;
        myApiConnector = apiConnector;
    }

    @NotNull
    @Override
    public String getContainerImage() {
        return myImageData.getDockerImage();
    }

    @NotNull
    @Override
    public ImagePullPolicy getImagePullPolicy() {
        return myImageData.getImagePullPolicy();
    }

    @Nullable
    @Override
    public String getContainerArguments() {
        return myImageData.getDockerArguments();
    }

    @Nullable
    @Override
    public String getContainerCommand() {
        return myImageData.getDockerCommand();
    }

    @NotNull
    @Override
    public String getId() {
        return myImageData.getId();
    }

    @NotNull
    @Override
    public String getName() {
        return myImageData.getName();
    }

    @NotNull
    @Override
    public Collection<? extends CloudInstance> getInstances() {
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public CloudInstance findInstanceById(@NotNull String s) {
        return null;
    }

    @Nullable
    @Override
    public Integer getAgentPoolId() {
        return myImageData.getAgentPoolId();
    }

    @Nullable
    @Override
    public CloudErrorInfo getErrorInfo() {
        return null;
    }
}
