package ekoshkin.teamcity.clouds.kubernetes;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import ekoshkin.teamcity.clouds.kubernetes.connector.ImagePullPolicy;
import ekoshkin.teamcity.clouds.kubernetes.connector.KubeApiConnector;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import jetbrains.buildServer.clouds.*;
import jetbrains.buildServer.serverSide.AgentDescription;
import jetbrains.buildServer.serverSide.ServerSettings;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static ekoshkin.teamcity.clouds.kubernetes.connector.KubeApiConnector.NEVER_RESTART_POLICY;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 27.05.17.
 */
public class KubeCloudClient implements CloudClientEx {
    private final KubeApiConnector myApiConnector;
    private final ServerSettings myServerSettings;
    private final ConcurrentHashMap<String, KubeCloudImage> myImageIdToImageMap;
    private final KubeCloudClientParameters myKubeClientParams;
    private CloudErrorInfo myCurrentError = null;

    public KubeCloudClient(@NotNull final KubeApiConnector apiConnector,
                           @NotNull ServerSettings serverSettings,
                           @NotNull List<KubeCloudImage> images,
                           @NotNull KubeCloudClientParameters kubeClientParams) {
        myApiConnector = apiConnector;
        myServerSettings = serverSettings;
        myImageIdToImageMap = new ConcurrentHashMap<String, KubeCloudImage>(Maps.uniqueIndex(images, new Function<KubeCloudImage, String>() {
            @NotNull
            @Override
            public String apply(@javax.annotation.Nullable KubeCloudImage kubeCloudImage) {
                return kubeCloudImage.getId();
            }
        }));
        myKubeClientParams = kubeClientParams;
    }

    @Override
    public boolean isInitialized() {
        //TODO: wait while all images populate list of their instances
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

        try {
            final Pod newPod = myApiConnector.createPod(podTemplate);
            myCurrentError = null;
            final KubeCloudInstanceImpl newInstance = new KubeCloudInstanceImpl(kubeCloudImage, newPod, myApiConnector);
            kubeCloudImage.addInstance(newInstance);
            return newInstance;
        } catch (KubernetesClientException ex){
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
        return myCurrentError;
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
        String agentNameProvided = cloudInstanceUserData.getAgentName();
        final String agentName = StringUtil.isEmpty(agentNameProvided) ? UUID.randomUUID().toString() : agentNameProvided;

        ImagePullPolicy imagePullPolicy = kubeCloudImage.getImagePullPolicy();
        String serverAddress = cloudInstanceUserData.getServerAddress();
        ContainerBuilder containerBuilder = new ContainerBuilder()
                .withName(agentName)
                .withImage(kubeCloudImage.getDockerImage())
                .withImagePullPolicy(imagePullPolicy == null ? ImagePullPolicy.IfNotPresent.getName() : imagePullPolicy.getName())
                .withEnv(new EnvVar(KubeContainerEnvironment.AGENT_NAME, agentName, null),
                        new EnvVar(KubeContainerEnvironment.SERVER_URL, serverAddress, null),
                        new EnvVar(KubeContainerEnvironment.OFFICIAL_IMAGE_SERVER_URL, serverAddress, null),
                        new EnvVar(KubeContainerEnvironment.IMAGE_NAME, kubeCloudImage.getName(), null),
                        new EnvVar(KubeContainerEnvironment.INSTANCE_NAME, agentName, null));
        String dockerCommand = kubeCloudImage.getDockerCommand();
        if(!StringUtil.isEmpty(dockerCommand)) containerBuilder = containerBuilder.withCommand(dockerCommand);
        String dockerArguments = kubeCloudImage.getDockerArguments();
        if(!StringUtil.isEmpty(dockerArguments)) containerBuilder = containerBuilder.withArgs(dockerArguments);

        return new PodBuilder()
                .withNewMetadata()
                .withName(agentName)
                .withNamespace(myKubeClientParams.getNamespace())
                .withLabels(CollectionsUtil.asMap(
                        KubeLabels.TEAMCITY_AGENT_LABEL,
                        KubeLabels.getServerLabel(myServerSettings.getServerUUID()),
                        KubeLabels.getProfileLabel(cloudInstanceUserData.getProfileId()),
                        KubeLabels.getImageLabel(kubeCloudImage.getId())))
                .endMetadata()
                .withNewSpec()
                .withContainers(Collections.singletonList(containerBuilder.build()))
                .withRestartPolicy(NEVER_RESTART_POLICY)
                .endSpec()
                .build();
    }
}