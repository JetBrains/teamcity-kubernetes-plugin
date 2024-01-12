
package jetbrains.buildServer.clouds.kubernetes.connector;

import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import java.util.Collection;
import java.util.Map;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FakeKubeApiConnector implements KubeApiConnector {


  public FakeKubeApiConnector(){}

  @NotNull
  @Override
  public KubeApiConnectionCheckResult testConnection() {
    return null;
  }

  @NotNull
  @Override
  public Pod createPod(@NotNull final Pod podTemplate) {
    return null;
  }

  @Override
  public PersistentVolumeClaim createPVC(@NotNull final PersistentVolumeClaim pvc) {
    return null;
  }

  @Override
  public boolean deletePod(@NotNull final String podName, final long gracePeriod) {
    return false;
  }

  @NotNull
  @Override
  public Collection<Pod> listPods(@NotNull final Map<String, String> labels) {
    return null;
  }

  @Override
  public String getNamespace() {
    return null;
  }

  @Nullable
  @Override
  public Deployment getDeployment(@NotNull final String deploymentName) {
    return null;
  }

  @Nullable
  @Override
  public PodStatus getPodStatus(@NotNull final String podName) {
    return null;
  }

  @NotNull
  @Override
  public Collection<String> listNamespaces() {
    return null;
  }

  @NotNull
  @Override
  public Collection<String> listDeployments() {
    return null;
  }

  @Override
  public boolean deletePVC(@NotNull final String name) {
    return false;
  }

  @Override
  public void invalidate() {
  }

  @Override
  public void close() {

  }
}