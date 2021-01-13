/*
 * Copyright 2000-2021 JetBrains s.r.o.
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

import com.google.common.collect.Maps;
import com.intellij.openapi.diagnostic.Logger;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import java.util.concurrent.ExecutorService;
import jetbrains.buildServer.agent.Constants;
import jetbrains.buildServer.clouds.*;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnector;
import jetbrains.buildServer.clouds.kubernetes.podSpec.BuildAgentPodTemplateProvider;
import jetbrains.buildServer.clouds.kubernetes.podSpec.BuildAgentPodTemplateProviders;
import jetbrains.buildServer.serverSide.AgentDescription;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static jetbrains.buildServer.clouds.kubernetes.KubeContainerEnvironment.IMAGE_NAME;
import static jetbrains.buildServer.clouds.kubernetes.KubeContainerEnvironment.INSTANCE_NAME;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 27.05.17.
 */
public class KubeCloudClient implements CloudClientEx {
    private static final String TEAMCITY_KUBE_PODS_GRACE_PERIOD = "teamcity.kube.pods.gracePeriod";
    private final static Logger LOG = Logger.getInstance(KubeCloudClient.class.getName());

    private final KubeApiConnector myApiConnector;
    private final ConcurrentHashMap<String, KubeCloudImage> myImageIdToImageMap;
    private final KubeCloudClientParametersImpl myKubeClientParams;
    private final KubeBackgroundUpdater myUpdater;
    private final KubePodNameGenerator myNameGenerator;
    private final BuildAgentPodTemplateProviders myPodTemplateProviders;
    private final ExecutorService myExecutorService;

    @Nullable private final String myServerUuid;
    private final String myCloudProfileId;

    public KubeCloudClient(@NotNull KubeApiConnector apiConnector,
                           @Nullable String serverUuid,
                           @NotNull String cloudProfileId,
                           @NotNull List<KubeCloudImage> images,
                           @NotNull KubeCloudClientParametersImpl kubeClientParams,
                           @NotNull KubeBackgroundUpdater updater,
                           @NotNull BuildAgentPodTemplateProviders podTemplateProviders,
                           @Nullable ExecutorService executorService,
                           @NotNull KubePodNameGenerator nameGenerator) {
        myApiConnector = apiConnector;
        myServerUuid = serverUuid;
        myCloudProfileId = cloudProfileId;
        myImageIdToImageMap = new ConcurrentHashMap<>(Maps.uniqueIndex(images, CloudImage::getId));
        myKubeClientParams = kubeClientParams;
        myPodTemplateProviders = podTemplateProviders;
        myExecutorService = executorService;
        myUpdater = updater;
        myNameGenerator = nameGenerator;
        myUpdater.registerClient(this);
    }

    @Override
    public boolean isInitialized() {
        return true;
    }

    @Override
    public void dispose() {
        LOG.debug("Disposing " + myCloudProfileId);
        myUpdater.unregisterClient(this);
        List<Runnable> runnables = myExecutorService.shutdownNow();
        if (runnables.size() > 0) {
            LOG.info(String.format("Forced shutdown of executor for '%s'. %d tasks might have been cancelled", myCloudProfileId, runnables.size()));
        }
        FileUtil.close(myApiConnector);
    }

    @NotNull
    @Override
    public CloudInstance startNewInstance(@NotNull CloudImage cloudImage, @NotNull CloudInstanceUserData cloudInstanceUserData) throws QuotaException {
        final KubeCloudImage kubeCloudImage = (KubeCloudImage)cloudImage;
        BuildAgentPodTemplateProvider podTemplateProvider = myPodTemplateProviders.get(kubeCloudImage.getPodSpecMode());
        final String instanceName = myNameGenerator.generateNewVmName(kubeCloudImage);
        final Pod podTemplate = podTemplateProvider.getPodTemplate(instanceName, cloudInstanceUserData, kubeCloudImage, myKubeClientParams);
        final PersistentVolumeClaim pvc = podTemplateProvider.getPVC(instanceName, kubeCloudImage);
        if (pvc != null){
            KubeTeamCityLabels.addCustomLabel(podTemplate, KubeTeamCityLabels.POD_PVC_NAME, pvc.getMetadata().getName());
        }
        KubeCloudInstance instance = new KubeCloudInstanceImpl(kubeCloudImage, podTemplate);
        kubeCloudImage.addStartedInstance(instance);
        myExecutorService.submit(() -> {
            try {
                if (pvc !=null){
                    myApiConnector.createPVC(pvc);
                }
                final Pod newPod = myApiConnector.createPod(podTemplate);
                instance.updateState(newPod);
            } catch (KubeCloudException | KubernetesClientException ex){
                instance.setStatus(InstanceStatus.ERROR);
                instance.setError(new CloudErrorInfo("Instance cannot be started", ex.getMessage(), ex));
                throw ex;
            }
        });
        return instance;
    }

    @Override
    public void restartInstance(@NotNull CloudInstance cloudInstance) {
        throw new UnsupportedOperationException("Restart not implemented");
    }

    @Override
    public void terminateInstance(@NotNull CloudInstance cloudInstance) {
        final KubeCloudInstance kubeCloudInstance = (KubeCloudInstance) cloudInstance;
        kubeCloudInstance.setStatus(InstanceStatus.SCHEDULED_TO_STOP);
        myExecutorService.submit(() -> {
            long gracePeriod = TeamCityProperties.getLong(TEAMCITY_KUBE_PODS_GRACE_PERIOD, 0);
            kubeCloudInstance.setStatus(InstanceStatus.STOPPING);
            try{
                int failedDeleteAttempts = 0;
                final String pvcName = kubeCloudInstance.getPVCName();
                while (!myApiConnector.deletePod(kubeCloudInstance.getName(), gracePeriod)){
                    failedDeleteAttempts++;
                    if(failedDeleteAttempts == 3) throw new KubeCloudException("Failed to delete pod " + kubeCloudInstance.getName());
                }
                failedDeleteAttempts = 0;
                while (pvcName != null && !myApiConnector.deletePVC(pvcName)){
                    failedDeleteAttempts++;
                    if(failedDeleteAttempts == 3) throw new KubeCloudException("Failed to delete PersistentVolumeClaim " + pvcName);
                }
                kubeCloudInstance.setError(null);
                kubeCloudInstance.setStatus(InstanceStatus.STOPPED);
            } catch (KubernetesClientException ex){
                kubeCloudInstance.setStatus(InstanceStatus.ERROR);
                kubeCloudInstance.setError(new CloudErrorInfo("Failed to terminate instance", ex.getMessage(), ex));
            }
            kubeCloudInstance.getImage().populateInstances();
        });
    }

    @Nullable
    @Override
    public CloudImage findImageById(@NotNull String imageId) throws CloudException {
        return myImageIdToImageMap.get(imageId);
    }

    @Nullable
    @Override
    public CloudInstance findInstanceByAgent(@NotNull AgentDescription agentDescription) {
        Map<String, String> agentParameters = agentDescription.getAvailableParameters();

        if((myServerUuid != null && !myServerUuid.equals(agentParameters.get(Constants.ENV_PREFIX + KubeContainerEnvironment.SERVER_UUID))) ||
                !myCloudProfileId.equals(agentParameters.get(Constants.ENV_PREFIX + KubeContainerEnvironment.PROFILE_ID)))
            return null;

        final String imageId = agentParameters.get(Constants.ENV_PREFIX + IMAGE_NAME);
        final String instanceName = agentParameters.get(Constants.ENV_PREFIX + INSTANCE_NAME);
        if (imageId != null) {
            final KubeCloudImage cloudImage = myImageIdToImageMap.get(imageId);
            if (cloudImage != null) {
                return cloudImage.findInstanceById(instanceName);
            }
        }
        return null;
    }

    @NotNull
    @Override
    public Collection<? extends CloudImage> getImages() throws CloudException {
        return Collections.unmodifiableCollection(myImageIdToImageMap.values());
    }

    @Nullable
    @Override
    public CloudErrorInfo getErrorInfo() {
        return null;
    }

    @Override
    public boolean canStartNewInstance(@NotNull CloudImage cloudImage) {
        KubeCloudImage kubeCloudImage = (KubeCloudImage) cloudImage;
        String kubeCloudImageId = kubeCloudImage.getId();
        if(!myImageIdToImageMap.containsKey(kubeCloudImageId)){
            LOG.debug("Can't start instance of unknown cloud image with id " + kubeCloudImageId);
            return false;
        }
        int profileInstanceLimit = myKubeClientParams.getInstanceLimit();
        if(profileInstanceLimit >= 0 && myImageIdToImageMap.values().stream().mapToInt(KubeCloudImage::getRunningInstanceCount).sum() >= profileInstanceLimit)
            return false;

        int imageLimit = kubeCloudImage.getInstanceLimit();
        return imageLimit < 0 || kubeCloudImage.getRunningInstanceCount() < imageLimit;
    }

    @Nullable
    @Override
    public String generateAgentName(@NotNull AgentDescription agentDescription) {
        final Map<String, String> agentParameters = agentDescription.getAvailableParameters();
        final String imageId = agentParameters.get(Constants.ENV_PREFIX + IMAGE_NAME);
        final String instanceName = agentParameters.get(Constants.ENV_PREFIX + INSTANCE_NAME);
        if (!StringUtil.isNotEmpty(imageId) || !StringUtil.isNotEmpty(instanceName))
            return null;

        final KubeCloudImage cloudImage = myImageIdToImageMap.get(imageId);
        if(cloudImage == null)
            return null;

        return cloudImage.getAgentName(instanceName);
    }

    public String getProfileId() {
        return myCloudProfileId;
    }
}