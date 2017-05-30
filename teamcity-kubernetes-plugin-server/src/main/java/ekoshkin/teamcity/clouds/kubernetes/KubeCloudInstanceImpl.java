package ekoshkin.teamcity.clouds.kubernetes;

import jetbrains.buildServer.clouds.CloudErrorInfo;
import jetbrains.buildServer.clouds.CloudImage;
import jetbrains.buildServer.clouds.InstanceStatus;
import jetbrains.buildServer.serverSide.AgentDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.Map;

import static ekoshkin.teamcity.clouds.kubernetes.KubeAgentProperties.INSTANCE_NAME;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 28.05.17.
 */
public class KubeCloudInstanceImpl implements KubeCloudInstance {
    private final KubeCloudImage myKubeCloudImage;

    public KubeCloudInstanceImpl(KubeCloudImage kubeCloudImage) {
        myKubeCloudImage = kubeCloudImage;
    }

    @NotNull
    @Override
    public String getInstanceId() {
        return null;
    }

    @NotNull
    @Override
    public String getName() {
        return null;
    }

    @NotNull
    @Override
    public String getImageId() {
        return null;
    }

    @NotNull
    @Override
    public CloudImage getImage() {
        return myKubeCloudImage;
    }

    @NotNull
    @Override
    public Date getStartedTime() {
        return null;
    }

    @Nullable
    @Override
    public String getNetworkIdentity() {
        return null;
    }

    @NotNull
    @Override
    public InstanceStatus getStatus() {
        return null;
    }

    @Nullable
    @Override
    public CloudErrorInfo getErrorInfo() {
        return null;
    }

    @Override
    public boolean containsAgent(@NotNull AgentDescription agentDescription) {
        final Map<String, String> configParams = agentDescription.getConfigurationParameters();
        return getInstanceId().equals(configParams.get(INSTANCE_NAME));
    }

    @Override
    public String getPodName() {
        return null;
    }
}
