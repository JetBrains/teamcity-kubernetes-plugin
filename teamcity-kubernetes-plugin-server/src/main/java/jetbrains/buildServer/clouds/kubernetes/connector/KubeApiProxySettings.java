package jetbrains.buildServer.clouds.kubernetes.connector;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface KubeApiProxySettings {
  @NotNull
  String getProxyUri();

  @Nullable
  String getProxyUsername();

  @Nullable
  String getProxyPassword();

  @NotNull
  String[] getNonProxyHosts();
}
