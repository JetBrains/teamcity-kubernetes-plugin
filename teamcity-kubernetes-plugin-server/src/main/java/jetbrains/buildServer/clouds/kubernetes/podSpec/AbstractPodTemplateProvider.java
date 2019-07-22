package jetbrains.buildServer.clouds.kubernetes.podSpec;

import com.intellij.openapi.util.Pair;
import io.fabric8.kubernetes.api.model.*;
import java.util.*;
import jetbrains.buildServer.clouds.CloudInstanceUserData;
import jetbrains.buildServer.clouds.kubernetes.KubeCloudClientParameters;
import jetbrains.buildServer.clouds.kubernetes.KubeCloudImage;
import jetbrains.buildServer.clouds.kubernetes.KubeContainerEnvironment;
import jetbrains.buildServer.clouds.kubernetes.KubeTeamCityLabels;
import jetbrains.buildServer.util.CollectionsUtil;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractPodTemplateProvider implements BuildAgentPodTemplateProvider {

  protected Pod patchedPodTemplateSpec(@NotNull PodTemplateSpec podTemplateSpec,
                                      @NotNull String instanceName,
                                      @NotNull String namespace,
                                      @NotNull String serverUUID,
                                      @NotNull String cloudProfileId,
                                      @NotNull String imageId,
                                      @NotNull String serverAddress
                                      ) {
    ObjectMeta metadata = podTemplateSpec.getMetadata();
    metadata.setName(instanceName);
    metadata.setNamespace(namespace);

    Map<String, String> patchedLabels = new HashMap<>();
    patchedLabels.putAll(metadata.getLabels());
    patchedLabels.putAll(CollectionsUtil.asMap(
      KubeTeamCityLabels.TEAMCITY_AGENT_LABEL, "",
      KubeTeamCityLabels.TEAMCITY_SERVER_UUID, serverUUID,
      KubeTeamCityLabels.TEAMCITY_CLOUD_PROFILE, cloudProfileId,
      KubeTeamCityLabels.TEAMCITY_CLOUD_IMAGE, imageId));
    metadata.setLabels(patchedLabels);

    PodSpec spec = podTemplateSpec.getSpec();
    for (Container container : spec.getContainers()) {
      container.setName(instanceName);

      Map<String, String> patchedEnvData = new HashMap<>();
      for (EnvVar env : container.getEnv()) {
        patchedEnvData.put(env.getName(), env.getValue());
      }

      for (Pair<String, String> env : Arrays.asList(
        new Pair<>(KubeContainerEnvironment.SERVER_UUID, serverUUID),
        new Pair<>(KubeContainerEnvironment.PROFILE_ID, cloudProfileId),
        new Pair<>(KubeContainerEnvironment.IMAGE_ID, imageId),
        new Pair<>(KubeContainerEnvironment.INSTANCE_NAME, instanceName))) {
        patchedEnvData.put(env.first, env.second);
      }

      if (!patchedEnvData.containsKey(KubeContainerEnvironment.SERVER_URL)) {
        patchedEnvData.put(KubeContainerEnvironment.SERVER_URL, serverAddress);
      }
      if (!patchedEnvData.containsKey(KubeContainerEnvironment.OFFICIAL_IMAGE_SERVER_URL)) {
        patchedEnvData.put(KubeContainerEnvironment.OFFICIAL_IMAGE_SERVER_URL, serverAddress);
      }

      List<EnvVar> patchedEnv = new ArrayList<>();
      for (String envName : patchedEnvData.keySet()) {
        patchedEnv.add(new EnvVar(envName, patchedEnvData.get(envName), null));
      }
      container.setEnv(patchedEnv);
    }
    return new PodBuilder().withMetadata(metadata).withSpec(spec).build();
  }

  @NotNull
  @Override
  public Pod getPodTemplate(@NotNull final CloudInstanceUserData cloudInstanceUserData,
                            @NotNull final KubeCloudImage kubeCloudImage,
                            @NotNull final KubeCloudClientParameters clientParameters) {
    throw new UnsupportedOperationException("AbstractPodTemplateProvider.getPodTemplate");
    //return null;
  }
}
