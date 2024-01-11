package jetbrains.buildServer.clouds.kubernetes.connection;

import io.fabric8.kubernetes.client.Config;
import jetbrains.buildServer.serverSide.connections.credentials.ConnectionCredentials;
import org.jetbrains.annotations.NotNull;

public interface KubernetesConnectionCredentials extends ConnectionCredentials {

  @NotNull
  @Override
  default String getProviderType() {
    return KubernetesConnectionConstants.CONNECTION_TYPE;
  }

  Config getCredentials();
}
