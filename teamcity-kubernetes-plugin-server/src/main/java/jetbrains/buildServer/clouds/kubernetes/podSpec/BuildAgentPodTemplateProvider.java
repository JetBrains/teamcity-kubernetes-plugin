
package jetbrains.buildServer.clouds.kubernetes.podSpec;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Pod;
import java.util.List;
import java.util.stream.Collectors;
import jetbrains.buildServer.agent.AgentRuntimeProperties;
import jetbrains.buildServer.clouds.CloudInstanceUserData;
import jetbrains.buildServer.clouds.kubernetes.KubeCloudImage;
import jetbrains.buildServer.clouds.kubernetes.KubeContainerEnvironment;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 15.06.17.
 */
public interface BuildAgentPodTemplateProvider {
    @NotNull String getId();
    @NotNull String getDisplayName();
    @Nullable String getDescription();

    @NotNull
    Pod getPodTemplate(@NotNull String instanceName,
                       @NotNull CloudInstanceUserData cloudInstanceUserData,
                       @NotNull KubeCloudImage kubeCloudImage,
                       @NotNull KubeApiConnector apiConnector);

    static @NotNull List<EnvVar> generateCustomAgentEnvVars(@NotNull CloudInstanceUserData cloudInstanceUserData) {
      return cloudInstanceUserData.getCustomAgentConfigurationParameters().entrySet().stream()
                                  .filter(entry -> !entry.getKey().startsWith(KubeContainerEnvironment.TEAMCITY_KUBERNETES_PREFIX))
                                  .filter(entry -> !entry.getKey().startsWith(KubeContainerEnvironment.TEAMCITY_KUBERNETES_PROVIDED_PREFIX))
                                  .filter(entry -> !entry.getKey().equals(AgentRuntimeProperties.STARTING_CLOUD_INSTANCE_ID))
                                  .map(entry ->
                                         new EnvVar(KubeContainerEnvironment.TEAMCITY_KUBERNETES_PROVIDED_PREFIX + KubeContainerEnvironment.paramToEnvVar(entry.getKey()),
                                                    entry.getValue(), null)
                                  ).collect(Collectors.toList());
    }

    @Nullable
    PersistentVolumeClaim getPVC(@NotNull String instanceName,
                                 @NotNull KubeCloudImage kubeCloudImage);
}