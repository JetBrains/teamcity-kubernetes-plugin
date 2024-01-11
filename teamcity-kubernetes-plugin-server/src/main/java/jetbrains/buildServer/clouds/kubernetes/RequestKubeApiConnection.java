package jetbrains.buildServer.clouds.kubernetes;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnection;
import jetbrains.buildServer.controllers.BasePropertiesBean;
import jetbrains.buildServer.internal.PluginPropertiesUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static jetbrains.buildServer.agent.Constants.SECURE_PROPERTY_PREFIX;
import static jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants.*;

public class RequestKubeApiConnection implements KubeApiConnection {
  protected final Map<String, String> myProps;

  public RequestKubeApiConnection(@NotNull HttpServletRequest request) {
    BasePropertiesBean propsBean =  new BasePropertiesBean(null);
    PluginPropertiesUtil.bindPropertiesFromRequest(request, propsBean, true);
    myProps = propsBean.getProperties();
  }
  @NotNull
  @Override
  public String getApiServerUrl() {
    return myProps.get(KubeParametersConstants.API_SERVER_URL);
  }

  @NotNull
  @Override
  public String getNamespace() {
    String explicitNameSpace = myProps.get(KUBERNETES_NAMESPACE);
    return StringUtil.isEmpty(explicitNameSpace) ? DEFAULT_NAMESPACE : explicitNameSpace;
  }

  @Nullable
  @Override
  public String getCustomParameter(@NotNull String parameterName) {
    return myProps.containsKey(parameterName) ? myProps.get(parameterName) : myProps.get(SECURE_PROPERTY_PREFIX + parameterName);
  }

  @Nullable
  @Override
  public String getCACertData() {
    return myProps.get(SECURE_PROPERTY_PREFIX + CA_CERT_DATA);
  }

  @NotNull
  @Override
  public String getAuthStrategy() {
    return myProps.get(KubeParametersConstants.AUTH_STRATEGY);
  }
}
