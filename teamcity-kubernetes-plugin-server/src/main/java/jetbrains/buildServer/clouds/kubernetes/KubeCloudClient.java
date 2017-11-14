package jetbrains.buildServer.clouds.kubernetes;

import com.google.common.collect.Maps;
import com.intellij.openapi.diagnostic.Logger;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClientException;
import jetbrains.buildServer.agent.Constants;
import jetbrains.buildServer.clouds.*;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnector;
import jetbrains.buildServer.clouds.kubernetes.podSpec.BuildAgentPodTemplateProvider;
import jetbrains.buildServer.clouds.kubernetes.podSpec.BuildAgentPodTemplateProviders;
import jetbrains.buildServer.serverSide.AgentDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 27.05.17.
 */
public class KubeCloudClient implements CloudClientEx {
    private final static Logger LOG = Logger.getInstance(KubeCloudClient.class.getName());

    private final KubeApiConnector myApiConnector;
    private final ConcurrentHashMap<String, KubeCloudImage> myImageIdToImageMap;
    private final KubeCloudClientParametersImpl myKubeClientParams;
    private final BuildAgentPodTemplateProviders myPodTemplateProviders;
    private final KubeDataCache myCache;
    private final KubeBackgroundUpdater myUpdater;

    @Nullable private final String myServerUuid;
    private final String myCloudProfileId;

    private CloudErrorInfo myCurrentError = null;

    public KubeCloudClient(@Nullable String serverUuid,
                           @NotNull String cloudProfileId,
                           @NotNull KubeApiConnector apiConnector,
                           @NotNull List<KubeCloudImage> images,
                           @NotNull KubeCloudClientParametersImpl kubeClientParams,
                           @NotNull BuildAgentPodTemplateProviders podTemplateProviders,
                           @NotNull KubeDataCache cache,
                           @NotNull KubeBackgroundUpdater updater) {
        myServerUuid = serverUuid;
        myCloudProfileId = cloudProfileId;
        myApiConnector = apiConnector;
        myImageIdToImageMap = new ConcurrentHashMap<>(Maps.uniqueIndex(images, CloudImage::getId));
        myKubeClientParams = kubeClientParams;
        myPodTemplateProviders = podTemplateProviders;
        myCache = cache;
        myUpdater = updater;
        myUpdater.registerClient(this);
    }

    @Override
    public boolean isInitialized() {
        myUpdater.unregisterClient(this);
        return true;
    }

    @Override
    public void dispose() {
        LOG.debug("KubeCloudClient disposed.");
    }

    @NotNull
    @Override
    public CloudInstance startNewInstance(@NotNull CloudImage cloudImage, @NotNull CloudInstanceUserData cloudInstanceUserData) throws QuotaException {
        final KubeCloudImage kubeCloudImage = (KubeCloudImage) cloudImage;
        BuildAgentPodTemplateProvider podTemplateProvider = myPodTemplateProviders.get(kubeCloudImage.getPodSpecMode());
        try {
            final Pod podTemplate = podTemplateProvider.getPodTemplate(cloudInstanceUserData, kubeCloudImage, myKubeClientParams);
            final Pod newPod = myApiConnector.createPod(podTemplate);
            myCurrentError = null;
            final KubeCloudInstance newInstance = new CachingKubeCloudInstance(new KubeCloudInstanceImpl(kubeCloudImage, newPod, myApiConnector), myCache);
            kubeCloudImage.populateInstances();
            return newInstance;
        } catch (KubeCloudException | KubernetesClientException ex){
            myCurrentError = new CloudErrorInfo("Failed to start pod", ex.getMessage(), ex);
            throw ex;
        }
    }

    @Override
    public void restartInstance(@NotNull CloudInstance cloudInstance) {
        throw new UnsupportedOperationException("Restart not implemented");
    }

    @Override
    public void terminateInstance(@NotNull CloudInstance cloudInstance) {
        KubeCloudInstance kubeCloudInstance = (KubeCloudInstance) cloudInstance;
        kubeCloudInstance.terminate();
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

        final String imageId = agentParameters.get(Constants.ENV_PREFIX + KubeContainerEnvironment.IMAGE_ID);
        final String instanceName = agentParameters.get(Constants.ENV_PREFIX + KubeContainerEnvironment.INSTANCE_NAME);
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
        return myCurrentError;
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
        if(profileInstanceLimit > 0 && myImageIdToImageMap.values().stream().mapToInt(KubeCloudImage::getRunningInstanceCount).sum() >= profileInstanceLimit)
            return false;

        int imageLimit = kubeCloudImage.getInstanceLimit();
        return imageLimit <= 0 || kubeCloudImage.getRunningInstanceCount() < imageLimit;
    }

    @Nullable
    @Override
    public String generateAgentName(@NotNull AgentDescription agentDescription) {
        return agentDescription.getAvailableParameters().get(Constants.ENV_PREFIX + KubeContainerEnvironment.AGENT_NAME);
    }
}