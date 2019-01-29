package jetbrains.buildServer.clouds.kubernetes;

import com.google.common.collect.Maps;
import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.agent.Constants;
import jetbrains.buildServer.clouds.*;
import jetbrains.buildServer.serverSide.AgentDescription;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static jetbrains.buildServer.clouds.kubernetes.KubeContainerEnvironment.IMAGE_ID;
import static jetbrains.buildServer.clouds.kubernetes.KubeContainerEnvironment.INSTANCE_NAME;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 27.05.17.
 */
public class KubeCloudClient implements CloudClientEx {
    private final static Logger LOG = Logger.getInstance(KubeCloudClient.class.getName());

    private final ConcurrentHashMap<String, KubeCloudImage> myImageIdToImageMap;
    private final KubeCloudClientParametersImpl myKubeClientParams;
    private final KubeBackgroundUpdater myUpdater;

    @Nullable private final String myServerUuid;
    private final String myCloudProfileId;

    public KubeCloudClient(@Nullable String serverUuid,
                           @NotNull String cloudProfileId,
                           @NotNull List<KubeCloudImage> images,
                           @NotNull KubeCloudClientParametersImpl kubeClientParams,
                           @NotNull KubeBackgroundUpdater updater) {
        myServerUuid = serverUuid;
        myCloudProfileId = cloudProfileId;
        myImageIdToImageMap = new ConcurrentHashMap<>(Maps.uniqueIndex(images, CloudImage::getId));
        myKubeClientParams = kubeClientParams;
        myUpdater = updater;
        myUpdater.registerClient(this);
    }

    @Override
    public boolean isInitialized() {
        return true;
    }

    @Override
    public void dispose() {
        LOG.debug("KubeCloudClient disposed.");
        myUpdater.unregisterClient(this);
    }

    @NotNull
    @Override
    public CloudInstance startNewInstance(@NotNull CloudImage cloudImage, @NotNull CloudInstanceUserData cloudInstanceUserData) throws QuotaException {
        return ((KubeCloudImage) cloudImage).startNewInstance(cloudInstanceUserData, myKubeClientParams);
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

        final String imageId = agentParameters.get(Constants.ENV_PREFIX + IMAGE_ID);
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
        if(profileInstanceLimit > 0 && myImageIdToImageMap.values().stream().mapToInt(KubeCloudImage::getRunningInstanceCount).sum() >= profileInstanceLimit)
            return false;

        int imageLimit = kubeCloudImage.getInstanceLimit();
        return imageLimit <= 0 || kubeCloudImage.getRunningInstanceCount() < imageLimit;
    }

    @Nullable
    @Override
    public String generateAgentName(@NotNull AgentDescription agentDescription) {
        final Map<String, String> agentParameters = agentDescription.getAvailableParameters();
        final String imageId = agentParameters.get(Constants.ENV_PREFIX + IMAGE_ID);
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