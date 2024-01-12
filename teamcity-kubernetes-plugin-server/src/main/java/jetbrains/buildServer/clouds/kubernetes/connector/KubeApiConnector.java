
package jetbrains.buildServer.clouds.kubernetes.connector;

import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import java.io.Closeable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 28.05.17.
 */
public interface KubeApiConnector extends Closeable {
    String NEVER_RESTART_POLICY = "Never";

    @NotNull
    KubeApiConnectionCheckResult testConnection();

    @NotNull
    Pod createPod(@NotNull Pod podTemplate);

    PersistentVolumeClaim createPVC(@NotNull PersistentVolumeClaim pvc);

    boolean deletePod(@NotNull String podName, long gracePeriod);

    @NotNull
    Collection<Pod> listPods(@NotNull Map<String, String> labels);

    @Nullable
    Deployment getDeployment(@NotNull String deploymentName);

    String getNamespace();

    @Nullable
    PodStatus getPodStatus(@NotNull String podName);

    @NotNull
    Collection<String> listNamespaces();

    @NotNull
    Collection<String> listDeployments();

    boolean deletePVC(@NotNull String name);

    void invalidate();
}