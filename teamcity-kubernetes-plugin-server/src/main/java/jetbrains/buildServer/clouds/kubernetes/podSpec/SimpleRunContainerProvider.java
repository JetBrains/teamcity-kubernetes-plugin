package jetbrains.buildServer.clouds.kubernetes.podSpec;

import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import java.util.Collections;
import jetbrains.buildServer.clouds.CloudConstants;
import jetbrains.buildServer.clouds.CloudInstanceUserData;
import jetbrains.buildServer.clouds.kubernetes.*;
import jetbrains.buildServer.clouds.kubernetes.connector.ImagePullPolicy;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnector;
import jetbrains.buildServer.serverSide.ServerSettings;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 15.06.17.
 */
public class SimpleRunContainerProvider implements BuildAgentPodTemplateProvider {
    public static final String ID = "simple";
    private final ServerSettings myServerSettings;
    private final KubePodNameGenerator myPodNameGenerator;

    public SimpleRunContainerProvider(@NotNull ServerSettings serverSettings, final KubePodNameGenerator podNameGenerator) {
        myServerSettings = serverSettings;
        myPodNameGenerator = podNameGenerator;
    }

    @NotNull
    @Override
    public String getId() {
        return ID;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Simply run single container";
    }

    @Nullable
    @Override
    public String getDescription() {
        return null;
    }

    @NotNull
    @Override
    public Pod getPodTemplate(@NotNull CloudInstanceUserData cloudInstanceUserData, @NotNull KubeCloudImage kubeCloudImage, @NotNull KubeCloudClientParameters clientParameters) {
        final String agentName = myPodNameGenerator.generateNewVmName(kubeCloudImage);

        ImagePullPolicy imagePullPolicy = kubeCloudImage.getImagePullPolicy();
        String serverAddress = cloudInstanceUserData.getServerAddress();
        String serverUUID = myServerSettings.getServerUUID();
        String cloudProfileId = cloudInstanceUserData.getProfileId();

        ContainerBuilder containerBuilder = new ContainerBuilder()
                .withName(agentName)
                .withImage(kubeCloudImage.getDockerImage())
                .withImagePullPolicy(imagePullPolicy == null ? ImagePullPolicy.IfNotPresent.getName() : imagePullPolicy.getName())
                .withEnv(new EnvVar(KubeContainerEnvironment.SERVER_URL, serverAddress, null),
                         new EnvVar(KubeContainerEnvironment.SERVER_UUID, serverUUID, null),
                         new EnvVar(KubeContainerEnvironment.OFFICIAL_IMAGE_SERVER_URL, serverAddress, null),
                         new EnvVar(KubeContainerEnvironment.IMAGE_ID, kubeCloudImage.getId(), null),
                         new EnvVar(KubeContainerEnvironment.PROFILE_ID, cloudProfileId, null),
                         new EnvVar(KubeContainerEnvironment.INSTANCE_NAME, agentName, null),
                         new EnvVar(KubeContainerEnvironment.BUILD_ID, cloudInstanceUserData.getCustomAgentConfigurationParameters().get(CloudConstants.BUILD_ID), null));

        String dockerCommand = kubeCloudImage.getDockerCommand();
        if(!StringUtil.isEmpty(dockerCommand)) containerBuilder = containerBuilder.withCommand(dockerCommand);
        String dockerArguments = kubeCloudImage.getDockerArguments();
        if(!StringUtil.isEmpty(dockerArguments)) containerBuilder = containerBuilder.withArgs(dockerArguments);

        return new PodBuilder()
                .withNewMetadata()
                .withName(agentName)
                .withNamespace(clientParameters.getNamespace())
                .withLabels(CollectionsUtil.asMap(
                        KubeTeamCityLabels.TEAMCITY_AGENT_LABEL, "",
                        KubeTeamCityLabels.TEAMCITY_SERVER_UUID, serverUUID,
                        KubeTeamCityLabels.TEAMCITY_CLOUD_PROFILE, cloudProfileId,
                        KubeTeamCityLabels.TEAMCITY_CLOUD_IMAGE, kubeCloudImage.getId()))
                .endMetadata()
                .withNewSpec()
                .withContainers(Collections.singletonList(containerBuilder.build()))
                .withRestartPolicy(KubeApiConnector.NEVER_RESTART_POLICY)
                .endSpec()
                .build();
    }
}
