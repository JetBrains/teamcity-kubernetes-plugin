
package jetbrains.buildServer.clouds.kubernetes.podSpec;

import com.intellij.openapi.util.Pair;
import io.fabric8.kubernetes.api.model.*;
import java.util.*;
import java.util.regex.Pattern;
import jetbrains.buildServer.clouds.CloudInstanceUserData;
import jetbrains.buildServer.clouds.kubernetes.*;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnector;
import jetbrains.buildServer.util.CollectionsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static jetbrains.buildServer.clouds.kubernetes.KubeContainerEnvironment.*;
import static jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants.*;

public abstract class AbstractPodTemplateProvider implements BuildAgentPodTemplateProvider {
  private static final Pattern ENV_VAR_NAME = Pattern.compile("([A-Z]+[_])*[A-Z]+");

  protected Pod patchedPodTemplateSpec(@NotNull PodTemplateSpec podTemplateSpec,
                                       @NotNull String instanceName,
                                       @NotNull String namespace,
                                       @NotNull String serverUUID,
                                       @NotNull String imageId,
                                       @NotNull CloudInstanceUserData cloudInstanceUserData
                                      ) {
    if (podTemplateSpec.getMetadata() == null) {
      podTemplateSpec.setMetadata(new ObjectMeta());
    }
    final ObjectMeta metadata = podTemplateSpec.getMetadata();
    patchMetadata(instanceName, namespace, serverUUID, imageId, cloudInstanceUserData, metadata);

    final PodSpec spec = podTemplateSpec.getSpec();

    spec.getContainers().forEach(
      container -> container.setEnv(getPatchedEnvVars(instanceName, serverUUID, imageId, cloudInstanceUserData, container.getEnv()))
    );

    final Pod pod = new PodBuilder().build();
    pod.setMetadata(metadata);
    pod.setSpec(spec);
    return pod;
  }

  @NotNull
  protected List<EnvVar> getPatchedEnvVars(@NotNull final String instanceName,
                                           @NotNull final String serverUUID,
                                           @NotNull final String imageId,
                                           @NotNull final CloudInstanceUserData cloudInstanceUserData,
                                           @NotNull final List<EnvVar> initialEnvData) {
    final Set<String> envNamesSet = new HashSet<>();
    for (EnvVar env : initialEnvData) {
      envNamesSet.add(env.getName());
    }

    final List<EnvVar> retval = new ArrayList<>(initialEnvData);

    final Map<String, String> customParams = cloudInstanceUserData.getCustomAgentConfigurationParameters();
    customParams.forEach((k, v)->{
      if (!envNamesSet.contains(k) && k.startsWith(TEAMCITY_KUBERNETES_PREFIX)){
        if (ENV_VAR_NAME.matcher(k).matches()) {
          retval.add(new EnvVar(k, v, null));
        }
      }
    });

    // check for run in Kubernetes:
    if ("true".equals(customParams.get(RUN_IN_KUBE_FEATURE))){
      //TODO uncheck when will work on that feature
      /*
      final String buildId = customParams.get(CloudConstants.BUILD_ID);
      if (StringUtil.isNotEmpty(buildId)){
        envDataMap.put(KubeContainerEnvironment.BUILD_ID, buildId);
      }
      */
    }

    for (Pair<String, String> env : Arrays.asList(
      new Pair<>(SERVER_UUID, serverUUID),
      new Pair<>(CLOUD_INSTANCE_HASH, cloudInstanceUserData.getCustomAgentConfigurationParameters().get(CLOUD_INSTANCE_HASH_PROP)),
      new Pair<>(PROFILE_ID, cloudInstanceUserData.getProfileId()),
      new Pair<>(IMAGE_NAME, imageId),
      new Pair<>(INSTANCE_NAME, instanceName))
    ) {
      if (!envNamesSet.contains(env.first)) {
        retval.add(new EnvVar(env.first, env.second, null));
      }
    }

    if (!envNamesSet.contains(SERVER_URL)) {
      retval.add(new EnvVar(SERVER_URL, cloudInstanceUserData.getServerAddress(), null));
    }
    if (!envNamesSet.contains(OFFICIAL_IMAGE_SERVER_URL)) {
      retval.add(new EnvVar(OFFICIAL_IMAGE_SERVER_URL, cloudInstanceUserData.getServerAddress(), null));
    }

    return retval;
  }

  private void patchMetadata(@NotNull final String instanceName,
                             @NotNull final String namespace,
                             @NotNull final String serverUUID,
                             @NotNull final String imageId,
                             @NotNull final CloudInstanceUserData cloudInstanceUserData,
                             @NotNull final ObjectMeta metadata) {
    metadata.setName(instanceName);
    metadata.setNamespace(namespace);

    Map<String, String> patchedLabels = new HashMap<>();
    if (metadata.getLabels() != null) {
      patchedLabels.putAll(metadata.getLabels());
    }
    patchedLabels.putAll(CollectionsUtil.asMap(
      KubeTeamCityLabels.TEAMCITY_AGENT_LABEL, "",
      KubeTeamCityLabels.TEAMCITY_SERVER_UUID, serverUUID,
      KubeTeamCityLabels.TEAMCITY_CLOUD_PROFILE, cloudInstanceUserData.getProfileId(),
      KubeTeamCityLabels.TEAMCITY_CLOUD_IMAGE, imageId));
    metadata.setLabels(patchedLabels);
  }

  @NotNull
  @Override
  public abstract Pod getPodTemplate(@NotNull String kubeInstanceName,
                                    @NotNull final CloudInstanceUserData cloudInstanceUserData,
                                    @NotNull final KubeCloudImage kubeCloudImage,
                                    @NotNull final KubeApiConnector apiConnector);

  @Nullable
  @Override
  public PersistentVolumeClaim getPVC(@NotNull final String instanceName,
                                      @NotNull final KubeCloudImage kubeCloudImage) {
    return null;
  }
}