package ekoshkin.teamcity.clouds.kubernetes;

import ekoshkin.teamcity.clouds.kubernetes.connector.KubeApiConnector;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClientException;
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
    @NotNull
    private final KubeApiConnector myApiConnector;
    private final Pod myPod;
    private CloudErrorInfo myCurrentError;
    private Date myStartedTime = new Date();

    public KubeCloudInstanceImpl(@NotNull KubeCloudImage kubeCloudImage,
                                 @NotNull Pod pod,
                                 @NotNull KubeApiConnector apiConnector) {
        myKubeCloudImage = kubeCloudImage;
        myApiConnector = apiConnector;
        myPod = pod;
    }

    @NotNull
    @Override
    public String getInstanceId() {
        return myPod.getMetadata().getName();
    }

    @NotNull
    @Override
    public String getName() {
        return myPod.getMetadata().getName();
    }

    @NotNull
    @Override
    public String getImageId() {
        return myKubeCloudImage.getId();
    }

    @NotNull
    @Override
    public CloudImage getImage() {
        return myKubeCloudImage;
    }

    @NotNull
    @Override
    public Date getStartedTime() {
        //TODO: make this work => return new Date(myPod.getStatus().getStartTime());
        return myStartedTime;
    }

    @Nullable
    @Override
    public String getNetworkIdentity() {
        return myPod.getStatus().getPodIP();
    }

    @NotNull
    @Override
    public InstanceStatus getStatus() {
        return InstanceStatusUtils.mapPodPhase(myApiConnector.getPodPhase(myPod));
    }

    @Nullable
    @Override
    public CloudErrorInfo getErrorInfo() {
        return myCurrentError;
    }

    @Override
    public boolean containsAgent(@NotNull AgentDescription agentDescription) {
        final Map<String, String> configParams = agentDescription.getConfigurationParameters();
        return getName().equals(configParams.get(INSTANCE_NAME));
    }

    @Override
    public void terminate() {
        try{
            myApiConnector.deletePod(myPod);
            myCurrentError = null;
            myKubeCloudImage.deleteInstance(this);
        } catch (KubernetesClientException ex){
            myCurrentError = new CloudErrorInfo("Failed to terminate instance", ex.getMessage(), ex);
        }
    }
}
