package jetbrains.buildServer.clouds.kubernetes;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodCondition;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.client.KubernetesClientException;
import jetbrains.buildServer.clouds.CloudErrorInfo;
import jetbrains.buildServer.clouds.CloudImage;
import jetbrains.buildServer.clouds.InstanceStatus;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnector;
import jetbrains.buildServer.clouds.kubernetes.connector.PodConditionType;
import jetbrains.buildServer.serverSide.AgentDescription;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static jetbrains.buildServer.clouds.kubernetes.KubeAgentProperties.INSTANCE_NAME;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 28.05.17.
 */
public class KubeCloudInstanceImpl implements KubeCloudInstance {
    private SimpleDateFormat myPodTransitionTimeFormat;
    private SimpleDateFormat myPodStartTimeFormat;

    private final KubeCloudImage myKubeCloudImage;
    private final KubeApiConnector myApiConnector;
    private final Pod myPod;

    private CloudErrorInfo myCurrentError;
    private Date myCreationTime;

    public KubeCloudInstanceImpl(@NotNull KubeCloudImage kubeCloudImage,
                                 @NotNull Pod pod,
                                 @NotNull KubeApiConnector apiConnector) {
        myKubeCloudImage = kubeCloudImage;
        myApiConnector = apiConnector;
        myPod = pod;
        final TimeZone utc = TimeZone.getTimeZone("UTC");
        myPodStartTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        myPodStartTimeFormat.setTimeZone(utc);
        myPodTransitionTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        myPodTransitionTimeFormat.setTimeZone(utc);
        myCreationTime = new Date();
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
        final PodStatus podStatus = myApiConnector.getPodStatus(myPod.getMetadata().getName());
        if(podStatus == null) return myCreationTime;
        try {
            final List<PodCondition> podConditions = podStatus.getConditions();
            if (podConditions != null && !podConditions.isEmpty()) {
                for (PodCondition podCondition : podConditions) {
                    if (PodConditionType.valueOf(podCondition.getType()) == PodConditionType.Ready)
                        return myPodTransitionTimeFormat.parse(podCondition.getLastTransitionTime());
                }
            }
            String startTime = podStatus.getStartTime();
            return !StringUtil.isEmpty(startTime) ? myPodStartTimeFormat.parse(startTime) : myCreationTime;
        } catch (ParseException e) {
            throw new KubeCloudException("Failed to get instance start date", e);
        }
    }

    @Nullable
    @Override
    public String getNetworkIdentity() {
        return myPod.getStatus().getPodIP();
    }

    @NotNull
    @Override
    public InstanceStatus getStatus() {
        return InstanceStatusUtils.mapPodPhase(myApiConnector.getPodPhase(myPod.getMetadata().getName()));
    }

    @Nullable
    @Override
    public CloudErrorInfo getErrorInfo() {
        return myCurrentError;
    }

    @Override
    public boolean containsAgent(@NotNull AgentDescription agentDescription) {
        final Map<String, String> agentParams = agentDescription.getConfigurationParameters();
        return getName().equals(agentParams.get(INSTANCE_NAME));
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
