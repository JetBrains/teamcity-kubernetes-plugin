
package jetbrains.buildServer.clouds.kubernetes.connector;

import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
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

    /**
     * Creates a custom resource (e.g. an XSmogVM) of the given type. The resource is created
     * in the connection's namespace unless the type is cluster-scoped.
     */
    @NotNull
    GenericKubernetesResource createCustomResource(@NotNull CustomResourceContext resourceContext, @NotNull GenericKubernetesResource resource);

    boolean deleteCustomResource(@NotNull CustomResourceContext resourceContext, @NotNull String name);

    @NotNull
    Collection<GenericKubernetesResource> listCustomResources(@NotNull CustomResourceContext resourceContext, @NotNull Map<String, String> labels);

    void invalidate();
}