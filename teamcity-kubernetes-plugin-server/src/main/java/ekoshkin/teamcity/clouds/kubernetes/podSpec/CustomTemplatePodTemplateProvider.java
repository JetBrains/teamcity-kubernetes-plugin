package ekoshkin.teamcity.clouds.kubernetes.podSpec;

import ekoshkin.teamcity.clouds.kubernetes.KubeCloudClientParameters;
import ekoshkin.teamcity.clouds.kubernetes.KubeCloudException;
import ekoshkin.teamcity.clouds.kubernetes.KubeCloudImage;
import ekoshkin.teamcity.clouds.kubernetes.KubeContainerEnvironment;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.utils.Serialization;
import jetbrains.buildServer.clouds.CloudInstanceUserData;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.UUID;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 15.06.17.
 */
public class CustomTemplatePodTemplateProvider implements PodTemplateProvider {
    @NotNull
    @Override
    public String getId() {
        return "custom-pod-template";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Use custom pod template";
    }

    @Nullable
    @Override
    public String getDescription() {
        return null;
    }

    @NotNull
    @Override
    public Pod getPodTemplate(@NotNull CloudInstanceUserData cloudInstanceUserData, @NotNull KubeCloudImage kubeCloudImage, @NotNull KubeCloudClientParameters kubeClientParams) {
        String customPodTemplateSpecContent = kubeCloudImage.getCustomPodTemplateSpec();
        if(StringUtil.isEmpty(customPodTemplateSpecContent))
            throw new KubeCloudException("Custom pod template spec is not specified for image " + kubeCloudImage.getId());

        PodTemplateSpec podTemplateSpec = Serialization.unmarshal(new ByteArrayInputStream(customPodTemplateSpecContent.getBytes()), PodTemplateSpec.class);

        final String agentNameProvided = cloudInstanceUserData.getAgentName();
        final String agentName = StringUtil.isEmpty(agentNameProvided) ? UUID.randomUUID().toString() : agentNameProvided;
        final String serverAddress = cloudInstanceUserData.getServerAddress();

        ObjectMeta metadata = podTemplateSpec.getMetadata();
        metadata.setName(agentName);
        metadata.setNamespace(kubeClientParams.getNamespace());
        PodSpec spec = podTemplateSpec.getSpec();
        for (Container container : spec.getContainers()){
            container.setName(agentName);
            container.setEnv(Arrays.asList(
                    new EnvVar(KubeContainerEnvironment.AGENT_NAME, agentName, null),
                    new EnvVar(KubeContainerEnvironment.SERVER_URL, serverAddress, null),
                    new EnvVar(KubeContainerEnvironment.OFFICIAL_IMAGE_SERVER_URL, serverAddress, null),
                    new EnvVar(KubeContainerEnvironment.IMAGE_NAME, kubeCloudImage.getName(), null),
                    new EnvVar(KubeContainerEnvironment.INSTANCE_NAME, agentName, null)));
        }
        return new PodBuilder()
                .withMetadata(metadata)
                .withSpec(spec)
                .build();
    }
}
