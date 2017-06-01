package ekoshkin.teamcity.clouds.kubernetes;

import ekoshkin.teamcity.clouds.kubernetes.connector.Deployment;
import ekoshkin.teamcity.clouds.kubernetes.connector.KubeApiConnector;
import ekoshkin.teamcity.clouds.kubernetes.connector.KubeApiException;
import ekoshkin.teamcity.clouds.kubernetes.connector.KubeObjectPatches;
import jetbrains.buildServer.clouds.*;
import jetbrains.buildServer.serverSide.AgentDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 27.05.17.
 */
public class KubeCloudClient implements CloudClientEx {
    private final KubeApiConnector myApiConnector;
    private final ConcurrentHashMap<String, KubeCloudImage> myImageIdToImageMap = new ConcurrentHashMap<String, KubeCloudImage>();

    public KubeCloudClient(KubeApiConnector apiConnector) {
        myApiConnector = apiConnector;
    }

    @Override
    public boolean isInitialized() {
        return true;
    }

    @Override
    public void dispose() {
    }

    @NotNull
    @Override
    public CloudInstance startNewInstance(@NotNull CloudImage cloudImage, @NotNull CloudInstanceUserData cloudInstanceUserData) throws QuotaException {
        KubeCloudImage kubeCloudImage = (KubeCloudImage) cloudImage;
        String deploymentName = kubeCloudImage.getDeploymentName();
        Deployment deployment;
        try {
            deployment = myApiConnector.findDeployment(deploymentName);
            //TODO: sync block
            if(deployment == null){
                myApiConnector.createDeployment(deploymentName, kubeCloudImage.getImage());
            }
            deployment = myApiConnector.patchDeployment(deploymentName, KubeObjectPatches.forDeploymentReplicas(deployment.getReplicas() + 1));
            return new KubeCloudInstanceImpl(kubeCloudImage);
        } catch (KubeApiException ex){
            throw new CloudException("Failed to start new cloud instance for image " + cloudImage.getName(), ex);
        }
    }

    @Override
    public void restartInstance(@NotNull CloudInstance cloudInstance) {
        throw new UnsupportedOperationException("Restart not implemented");
    }

    @Override
    public void terminateInstance(@NotNull CloudInstance cloudInstance) {
        KubeCloudInstance kubeCloudInstance = (KubeCloudInstance) cloudInstance;
        myApiConnector.deletePod(kubeCloudInstance.getPodName());
        //TODO: update instance counter
    }

    @Nullable
    @Override
    public CloudImage findImageById(@NotNull String imageId) throws CloudException {
        return myImageIdToImageMap.get(imageId);
    }

    @Nullable
    @Override
    public CloudInstance findInstanceByAgent(@NotNull AgentDescription agentDescription) {
        final String imageName = agentDescription.getAvailableParameters().get(KubeAgentProperties.IMAGE_NAME);
        if (imageName != null) {
            final KubeCloudImage cloudImage = myImageIdToImageMap.get(imageName);
            if (cloudImage != null) {
                return cloudImage.findInstanceById(agentDescription.getAvailableParameters().get(KubeAgentProperties.INSTANCE_NAME));
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
        //TODO: count instances, look into kubeapi
        //TODO: introdice limit
        return true;
    }

    @Nullable
    @Override
    public String generateAgentName(@NotNull AgentDescription agentDescription) {
        return agentDescription.getAvailableParameters().get(KubeAgentProperties.INSTANCE_NAME);
    }
}