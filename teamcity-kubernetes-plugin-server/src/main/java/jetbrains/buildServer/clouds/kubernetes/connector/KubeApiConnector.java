package jetbrains.buildServer.clouds.kubernetes.connector;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 28.05.17.
 */
public interface KubeApiConnector {
    String NEVER_RESTART_POLICY = "Never";

    @NotNull
    KubeApiConnectionCheckResult testConnection();

    @NotNull
    Pod createPod(@NotNull Pod podTemplate);

    boolean deletePod(@NotNull Pod pod);

    @NotNull
    Collection<Pod> listPods(@NotNull Map<String, String> labels);

    @NotNull
    PodPhase getPodPhase(@NotNull String podName);

    @Nullable
    Deployment getDeployment(@NotNull String deploymentName);

    @Nullable
    PodStatus getPodStatus(@NotNull String podName);

    @NotNull
    Collection<String> listNamespaces();
}
