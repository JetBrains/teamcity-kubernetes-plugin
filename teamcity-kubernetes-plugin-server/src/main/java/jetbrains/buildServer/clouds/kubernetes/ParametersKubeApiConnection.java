package jetbrains.buildServer.clouds.kubernetes;

import java.util.Map;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnection;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants.*;

public class ParametersKubeApiConnection implements KubeApiConnection {
  protected final Map<String, String> myParameters;

  public ParametersKubeApiConnection(Map<String, String> parameters) {
    myParameters = parameters;
  }

  @Nullable
  @Override
  public String getApiServerUrl() {
    // can be null only if KubeAuthStrategy.requiresServerUrl is false
    return myParameters.get(API_SERVER_URL);
  }

  @NotNull
  @Override
  public String getNamespace() {
    String explicitNameSpace = myParameters.get(KUBERNETES_NAMESPACE);
    return StringUtil.isEmpty(explicitNameSpace) ? DEFAULT_NAMESPACE : explicitNameSpace;
  }

  @Nullable
  @Override
  public String getCustomParameter(@NotNull String parameterName) {
    return myParameters.get(parameterName);
  }

  @Nullable
  @Override
  public String getCACertData() {
    return myParameters.get(CA_CERT_DATA);
  }

  @Override
  @NotNull
  public String getAuthStrategy() {
    return myParameters.get(AUTH_STRATEGY);
  }
}
