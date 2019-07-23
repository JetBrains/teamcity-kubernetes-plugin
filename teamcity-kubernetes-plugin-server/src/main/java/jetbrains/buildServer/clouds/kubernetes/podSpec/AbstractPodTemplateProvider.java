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
                                       @NotNull String imageId,
                                       @NotNull CloudInstanceUserData cloudInstanceUserData
                                      ) {
    final ObjectMeta metadata = podTemplateSpec.getMetadata();
    patchMetadata(instanceName, namespace, serverUUID, imageId, cloudInstanceUserData, metadata);

    final PodSpec spec = podTemplateSpec.getSpec();

    spec.getContainers().forEach(
      container -> container.setEnv(patchEnvVars(instanceName, serverUUID, imageId, cloudInstanceUserData, container.getEnv()))
    );

    return new PodBuilder().withMetadata(metadata).withSpec(spec).build();
  }

  @NotNull
  private List<EnvVar> patchEnvVars(@NotNull final String instanceName,
                                    @NotNull final String serverUUID,
                                    @NotNull final String imageId,
                                    @NotNull final CloudInstanceUserData cloudInstanceUserData,
                                    final List<EnvVar> containerEnvData) {
    final Map<String, String> envDataMap = new HashMap<>();
    for (EnvVar env : containerEnvData) {
      envDataMap.put(env.getName(), env.getValue());
    }

    for (Pair<String, String> env : Arrays.asList(
      new Pair<>(KubeContainerEnvironment.SERVER_UUID, serverUUID),
      new Pair<>(KubeContainerEnvironment.PROFILE_ID, cloudInstanceUserData.getProfileId()),
      new Pair<>(KubeContainerEnvironment.IMAGE_ID, imageId),
      new Pair<>(KubeContainerEnvironment.INSTANCE_NAME, instanceName))
    ) {
      envDataMap.put(env.first, env.second);
    }

    cloudInstanceUserData.getCustomAgentConfigurationParameters().forEach((k,v)->{
      if (!envDataMap.containsKey(k) && k.startsWith(KubeContainerEnvironment.TEAMCITY_KUBERNETES_PREFIX)){
        envDataMap.put(k, v);
      }
    });

    if (!envDataMap.containsKey(KubeContainerEnvironment.SERVER_URL)) {
      envDataMap.put(KubeContainerEnvironment.SERVER_URL, cloudInstanceUserData.getServerAddress());
    }
    if (!envDataMap.containsKey(KubeContainerEnvironment.OFFICIAL_IMAGE_SERVER_URL)) {
      envDataMap.put(KubeContainerEnvironment.OFFICIAL_IMAGE_SERVER_URL, cloudInstanceUserData.getServerAddress());
    }

    final List<EnvVar> patchedEnv = new ArrayList<>();
    for (String envName : envDataMap.keySet()) {
      patchedEnv.add(new EnvVar(envName, envDataMap.get(envName), null));
    }
    return patchedEnv;
  }

  private void patchMetadata(@NotNull final String instanceName,
                             @NotNull final String namespace,
                             @NotNull final String serverUUID,
                             @NotNull final String imageId,
                             @NotNull final CloudInstanceUserData cloudInstanceUserData, final ObjectMeta metadata) {
    metadata.setName(instanceName);
    metadata.setNamespace(namespace);

    Map<String, String> patchedLabels = new HashMap<>();
    patchedLabels.putAll(metadata.getLabels());
    patchedLabels.putAll(CollectionsUtil.asMap(
      KubeTeamCityLabels.TEAMCITY_AGENT_LABEL, "",
      KubeTeamCityLabels.TEAMCITY_SERVER_UUID, serverUUID,
      KubeTeamCityLabels.TEAMCITY_CLOUD_PROFILE, cloudInstanceUserData.getProfileId(),
      KubeTeamCityLabels.TEAMCITY_CLOUD_IMAGE, imageId));
    metadata.setLabels(patchedLabels);
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
