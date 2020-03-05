/*
 * Copyright 2000-2020 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.clouds.kubernetes.podSpec;

import com.intellij.openapi.util.Pair;
import io.fabric8.kubernetes.api.model.*;
import java.util.*;
import java.util.regex.Pattern;
import jetbrains.buildServer.clouds.CloudInstanceUserData;
import jetbrains.buildServer.clouds.kubernetes.*;
import jetbrains.buildServer.util.CollectionsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static jetbrains.buildServer.clouds.kubernetes.KubeContainerEnvironment.*;
import static jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants.*;
import static jetbrains.buildServer.clouds.kubernetes.KubeTeamCityLabels.*;

public abstract class AbstractPodTemplateProvider implements BuildAgentPodTemplateProvider {
  private static final Pattern ENV_VAR_NAME = Pattern.compile("([A-Z]+[_])*[A-Z]+");

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

    final Pod pod = new PodBuilder().build();
    pod.setMetadata(metadata);
    pod.setSpec(spec);
    return pod;
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
      new Pair<>(SERVER_UUID, serverUUID),
      new Pair<>(PROFILE_ID, cloudInstanceUserData.getProfileId()),
      new Pair<>(IMAGE_NAME, imageId),
      new Pair<>(INSTANCE_NAME, instanceName))
    ) {
      envDataMap.put(env.first, env.second);
    }

    final Map<String, String> customParams = cloudInstanceUserData.getCustomAgentConfigurationParameters();
    customParams.forEach((k, v)->{
      if (!envDataMap.containsKey(k) && k.startsWith(TEAMCITY_KUBERNETES_PREFIX)){
        if (ENV_VAR_NAME.matcher(k).matches()) {
          envDataMap.put(k, v);
        } else {
          // Do noting
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

    if (!envDataMap.containsKey(SERVER_URL)) {
      envDataMap.put(SERVER_URL, cloudInstanceUserData.getServerAddress());
    }
    if (!envDataMap.containsKey(OFFICIAL_IMAGE_SERVER_URL)) {
      envDataMap.put(OFFICIAL_IMAGE_SERVER_URL, cloudInstanceUserData.getServerAddress());
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
                                    @NotNull final KubeCloudClientParameters clientParameters);

  @Nullable
  @Override
  public PersistentVolumeClaim getPVC(@NotNull final String instanceName,
                                      @NotNull final KubeCloudImage kubeCloudImage) {
    return null;
  }
}
