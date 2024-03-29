
package jetbrains.buildServer.clouds.kubernetes;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodCondition;
import io.fabric8.kubernetes.api.model.PodStatus;
import jetbrains.buildServer.clouds.CloudErrorInfo;
import jetbrains.buildServer.clouds.InstanceStatus;
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

import static jetbrains.buildServer.clouds.kubernetes.KubeContainerEnvironment.INSTANCE_NAME;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 28.05.17.
 */
public class KubeCloudInstanceImpl implements KubeCloudInstance {

    private SimpleDateFormat myPodTransitionTimeFormat;
    private SimpleDateFormat myPodStartTimeFormat;

    private final KubeCloudImage myKubeCloudImage;
    @Nullable
    private volatile Pod myPod;
    private volatile InstanceStatus myInstanceStatus = InstanceStatus.SCHEDULED_TO_START;
    private volatile Date myStartDate;

    private CloudErrorInfo myCurrentError;
    private Date myCreationTime;

    public KubeCloudInstanceImpl(@NotNull KubeCloudImage kubeCloudImage, @NotNull Pod pod) {
        myKubeCloudImage = kubeCloudImage;
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
    public KubeCloudImage getImage() {
        return myKubeCloudImage;
    }

    @Override
    public void setStatus(final InstanceStatus status) {
        myInstanceStatus = status;
    }

    @NotNull
    @Override
    public Date getStartedTime() {
        final PodStatus podStatus = myPod.getStatus();
        if(podStatus == null) return myCreationTime;
        try {
            final List<PodCondition> podConditions = podStatus.getConditions();
            if (podConditions != null && !podConditions.isEmpty()) {
                for (PodCondition podCondition : podConditions) {
                    if (PodConditionType.fromString(podCondition.getType()) == PodConditionType.Ready)
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
        if (myPod.getStatus() == null)
            return null;
        return myPod.getStatus().getPodIP();
    }

    @NotNull
    @Override
    public InstanceStatus getStatus() {
        return myInstanceStatus;
    }

    @Nullable
    @Override
    public CloudErrorInfo getErrorInfo() {
        return myCurrentError;
    }

    @Override
    public boolean containsAgent(@NotNull AgentDescription agentDescription) {
        final Map<String, String> buildParams = agentDescription.getBuildParameters();
        return getName().equals(buildParams.get("env."+INSTANCE_NAME));
    }

    public void updateState(@NotNull Pod actualPod){
        myPod = actualPod;
        InstanceStatus podStatus = KubeUtils.mapPodPhase(actualPod.getStatus());
        myCurrentError = KubeUtils.getErrorMessage(myPod.getStatus());
        if (!getStatus().isCanTerminate()) { // don't update status if instance is going to be terminated
            return;
        }
        if (podStatus == InstanceStatus.STOPPED) {
            setStatus(InstanceStatus.STOPPED);
        } else if (KubeUtils.isPodStatus(myInstanceStatus)){
            setStatus(podStatus);
        }
    }

    @Override
    public void setError(@Nullable final CloudErrorInfo errorInfo) {
        myCurrentError = errorInfo;
    }

    @Override
    @Nullable
    public String getPVCName() {
        if (myPod.getMetadata() == null ||  myPod.getMetadata().getLabels() == null)
            return null;
        return myPod.getMetadata().getLabels().get(KubeTeamCityLabels.POD_PVC_NAME);
    }

}