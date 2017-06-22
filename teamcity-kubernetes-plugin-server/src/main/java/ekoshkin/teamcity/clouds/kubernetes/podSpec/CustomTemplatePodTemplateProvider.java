package ekoshkin.teamcity.clouds.kubernetes.podSpec;

import com.intellij.openapi.util.Pair;
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
import java.util.*;

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

            Map<String, String> patchedEnvData = new HashMap<>();
            for (EnvVar env : container.getEnv()){
                patchedEnvData.put(env.getName(), env.getValue());
            }

            for (Pair<String, String> env : Arrays.asList(
                    new Pair<>(KubeContainerEnvironment.AGENT_NAME, agentName),
                    new Pair<>(KubeContainerEnvironment.SERVER_URL, serverAddress),
                    new Pair<>(KubeContainerEnvironment.OFFICIAL_IMAGE_SERVER_URL, serverAddress),
                    new Pair<>(KubeContainerEnvironment.IMAGE_NAME, kubeCloudImage.getName()),
                    new Pair<>(KubeContainerEnvironment.INSTANCE_NAME, agentName))){
                patchedEnvData.put(env.first, env.second);
            }

            List<EnvVar> patchedEnv = new ArrayList<>();
            for (String envName : patchedEnvData.keySet()){
                patchedEnv.add(new EnvVar(envName, patchedEnvData.get(envName), null));
            }
            container.setEnv(patchedEnv);
        }
        return new PodBuilder()
                .withMetadata(metadata)
                .withSpec(spec)
                .build();
    }
}
