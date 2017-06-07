package ekoshkin.teamcity.clouds.kubernetes;

import ekoshkin.teamcity.clouds.kubernetes.connector.KubeApiConnector;
import io.fabric8.kubernetes.api.model.*;
import jetbrains.buildServer.clouds.*;
import jetbrains.buildServer.serverSide.AgentDescription;
import jetbrains.buildServer.serverSide.ServerSettings;
import jetbrains.buildServer.util.CollectionsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import static ekoshkin.teamcity.clouds.kubernetes.connector.KubeApiConnector.*;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 27.05.17.
 */
public class KubeCloudClient implements CloudClientEx {
    private final KubeApiConnector myApiConnector;
    @NotNull
    private final ServerSettings myServerSettings;
    private final ConcurrentHashMap<String, KubeCloudImage> myImageIdToImageMap = new ConcurrentHashMap<String, KubeCloudImage>();

    public KubeCloudClient(@NotNull KubeApiConnector apiConnector,
                           @NotNull ServerSettings serverSettings,
                           @NotNull KubeCloudClientParameters kubeCloudClientParameters) {
        myApiConnector = apiConnector;
        myServerSettings = serverSettings;
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
        final KubeCloudImage kubeCloudImage = (KubeCloudImage) cloudImage;
        final Pod podTemplate = getPodTemplate(cloudInstanceUserData, kubeCloudImage);
        final Pod newPod = myApiConnector.createPod(podTemplate);
        return new KubeCloudInstanceImpl(kubeCloudImage, newPod);
    }

    @Override
    public void restartInstance(@NotNull CloudInstance cloudInstance) {
        throw new UnsupportedOperationException("Restart not implemented");
    }

    @Override
    public void terminateInstance(@NotNull CloudInstance cloudInstance) {
        KubeCloudInstance kubeCloudInstance = (KubeCloudInstance) cloudInstance;
        myApiConnector.deletePod(kubeCloudInstance.getPod());
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

    private Pod getPodTemplate(@NotNull CloudInstanceUserData cloudInstanceUserData, KubeCloudImage kubeCloudImage) {
        final String agentName = cloudInstanceUserData.getAgentName(); //TODO: review agent name generation

        Container container = new ContainerBuilder()
                .withName(agentName) //TODO: review
                .withImage(kubeCloudImage.getContainerImage())
                .withImagePullPolicy(kubeCloudImage.isAlwaysPullImage() ? ALWAYS_PULL_IMAGE_POLICY : IF_NOT_PRESENT_PULL_IMAGE_POLICY)
                .withArgs(kubeCloudImage.getContainerArguments())
                .withCommand(kubeCloudImage.getContainerCommand())
                .withEnv(new EnvVar(KubeContainerEnvironment.AGENT_NAME, agentName, null))
                .withEnv(new EnvVar(KubeContainerEnvironment.SERVER_URL, cloudInstanceUserData.getServerAddress(), null))
                .withEnv(new EnvVar(KubeContainerEnvironment.IMAGE_NAME, kubeCloudImage.getName(), null))
                .withEnv(new EnvVar(KubeContainerEnvironment.INSTANCE_NAME, agentName, null)) //TODO: review
                .build();

        return new PodBuilder()
                .withNewMetadata()
                .withName(agentName) //TODO: review
                .withLabels(CollectionsUtil.asMap(
                        KubeLabels.TEAMCITY_AGENT_LABEL,
                        KubeLabels.getServerLabel(myServerSettings.getServerUUID()),
                        KubeLabels.getProfileLabel(cloudInstanceUserData.getProfileId()),
                        KubeLabels.getImageLabel(kubeCloudImage.getId())))
                .endMetadata()
                .withNewSpec()
                .withContainers(Collections.singletonList(container))
                .withRestartPolicy(NEVER_RESTART_POLICY)
                .endSpec()
                .build();
    }
}