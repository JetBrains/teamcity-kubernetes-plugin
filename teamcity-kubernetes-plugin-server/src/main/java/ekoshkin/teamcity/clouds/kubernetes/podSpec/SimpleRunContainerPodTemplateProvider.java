package ekoshkin.teamcity.clouds.kubernetes.podSpec;

import ekoshkin.teamcity.clouds.kubernetes.KubeCloudClientParameters;
import ekoshkin.teamcity.clouds.kubernetes.KubeCloudImage;
import ekoshkin.teamcity.clouds.kubernetes.KubeContainerEnvironment;
import ekoshkin.teamcity.clouds.kubernetes.KubeTeamCityLabels;
import ekoshkin.teamcity.clouds.kubernetes.connector.ImagePullPolicy;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import jetbrains.buildServer.clouds.CloudInstanceUserData;
import jetbrains.buildServer.serverSide.ServerSettings;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.UUID;

import static ekoshkin.teamcity.clouds.kubernetes.connector.KubeApiConnector.NEVER_RESTART_POLICY;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 15.06.17.
 */
public class SimpleRunContainerPodTemplateProvider implements PodTemplateProvider {
    private final ServerSettings myServerSettings;

    public SimpleRunContainerPodTemplateProvider(@NotNull ServerSettings serverSettings) {
        myServerSettings = serverSettings;
    }

    @NotNull
    @Override
    public String getId() {
        return "simple";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Simply run container";
    }

    @Nullable
    @Override
    public String getDescription() {
        return null;
    }

    @NotNull
    @Override
    public Pod getPodTemplate(@NotNull CloudInstanceUserData cloudInstanceUserData, @NotNull KubeCloudImage kubeCloudImage, @NotNull KubeCloudClientParameters clientParameters) {
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
                .withNamespace(clientParameters.getNamespace())
                .withLabels(CollectionsUtil.asMap(
                        KubeTeamCityLabels.TEAMCITY_AGENT_LABEL, "",
                        KubeTeamCityLabels.TEAMCITY_SERVER_UUID, myServerSettings.getServerUUID(),
                        KubeTeamCityLabels.TEAMCITY_CLOUD_PROFILE, cloudInstanceUserData.getProfileId(),
                        KubeTeamCityLabels.TEAMCITY_CLOUD_IMAGE, kubeCloudImage.getId()))
                .endMetadata()
                .withNewSpec()
                .withContainers(Collections.singletonList(containerBuilder.build()))
                .withRestartPolicy(NEVER_RESTART_POLICY)
                .endSpec()
                .build();
    }
}
