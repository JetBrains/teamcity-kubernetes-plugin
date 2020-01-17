/*
 * Copyright 2000-2020 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import jetbrains.buildServer.serverSide.TeamCityProperties;
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
    private static final String TEAMCITY_KUBE_PODS_GRACE_PERIOD = "teamcity.kube.pods.gracePeriod";

    private SimpleDateFormat myPodTransitionTimeFormat;
    private SimpleDateFormat myPodStartTimeFormat;

    private final KubeCloudImage myKubeCloudImage;
    private final KubeApiConnector myApiConnector;
    private volatile Pod myPod;
    private volatile InstanceStatus myInstanceStatus;
    private volatile Date myStartDate;

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
        final PodStatus podStatus = myPod.getStatus();
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
        return  InstanceStatusUtils.mapPodPhase(myPod.getStatus());
    }

    @Nullable
    @Override
    public CloudErrorInfo getErrorInfo() {
        return InstanceStatusUtils.getErrorMessage(myPod.getStatus());
    }

    @Override
    public boolean containsAgent(@NotNull AgentDescription agentDescription) {
        final Map<String, String> agentParams = agentDescription.getConfigurationParameters();
        return getName().equals(agentParams.get(INSTANCE_NAME));
    }

    @Override
    public void terminate() {
        long gracePeriod = TeamCityProperties.getLong(TEAMCITY_KUBE_PODS_GRACE_PERIOD, 0);
        try{
            int failedDeleteAttempts = 0;
            while (!myApiConnector.deletePod(myPod, gracePeriod)){
                failedDeleteAttempts++;
                if(failedDeleteAttempts == 10) throw new KubeCloudException("Failed to delete pod " + myPod);
            }
            myCurrentError = null;
        } catch (KubernetesClientException ex){
            myCurrentError = new CloudErrorInfo("Failed to terminate instance", ex.getMessage(), ex);
        }
        myKubeCloudImage.populateInstances();
    }

    public void updateState(@NotNull Pod actualPod){
        myPod = actualPod;
    }
}
