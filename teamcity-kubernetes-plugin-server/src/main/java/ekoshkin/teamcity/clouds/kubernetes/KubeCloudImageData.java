package ekoshkin.teamcity.clouds.kubernetes;

import ekoshkin.teamcity.clouds.kubernetes.connector.ImagePullPolicy;
import jetbrains.buildServer.clouds.CloudImageParameters;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 12.06.17.
 */
public class KubeCloudImageData {
    private final CloudImageParameters myRawImageData;
    private String myCustomPodTemplateContent;

    public KubeCloudImageData(@NotNull final CloudImageParameters rawImageData) {
        myRawImageData = rawImageData;
    }

    public String getDockerImage() {
        return myRawImageData.getParameter(KubeParametersConstants.DOCKER_IMAGE);
    }

    @Nullable
    public ImagePullPolicy getImagePullPolicy() {
        final String parameter = myRawImageData.getParameter(KubeParametersConstants.IMAGE_PULL_POLICY);
        return StringUtil.isEmpty(parameter) ? null : ImagePullPolicy.valueOf(parameter);
    }

    public String getDockerArguments() {
        return myRawImageData.getParameter(KubeParametersConstants.DOCKER_ARGS);
    }

    public String getDockerCommand() {
        return myRawImageData.getParameter(KubeParametersConstants.DOCKER_CMD);
    }

    public String getId() {
        return myRawImageData.getId();
    }

    public String getName() {
        return String.format("%s (id: %s)", getDockerImage(), getId());
    }

    public Integer getAgentPoolId() {
        return myRawImageData.getAgentPoolId();
    }

    public String getPodSpecMode() {
        return myRawImageData.getParameter(KubeParametersConstants.POD_TEMPLATE_MODE);
    }

    public String getCustomPodTemplateContent() {
        return myRawImageData.getParameter(KubeParametersConstants.CUSTOM_POD_TEMPLATE);
    }
}
