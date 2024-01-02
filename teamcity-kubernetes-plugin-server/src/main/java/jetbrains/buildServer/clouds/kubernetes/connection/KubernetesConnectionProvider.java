package jetbrains.buildServer.clouds.kubernetes.connection;

import java.util.Map;
import jetbrains.buildServer.serverSide.oauth.OAuthProvider;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KubernetesConnectionProvider extends OAuthProvider {
  private final PluginDescriptor myPluginDescriptor;

  public KubernetesConnectionProvider(PluginDescriptor pluginDescriptor) {
    myPluginDescriptor = pluginDescriptor;
  }

  @NotNull
  @Override
  public String getType() {
    return KubernetesConnectionConstants.CONNECTION_TYPE;
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return "Kubernetes Connection";
  }

  @NotNull
  @Override
  public String describeConnection(@NotNull Map<String, String> connectionProperties) {
    return super.describeConnection(connectionProperties);
  }

  @Nullable
  @Override
  public String getEditParametersUrl() {
    return myPluginDescriptor.getPluginResourcesPath("editConnection.jsp");
  }

  @Nullable
  @Override
  public Map<String, String> getDefaultProperties() {
    return super.getDefaultProperties();
  }
}
