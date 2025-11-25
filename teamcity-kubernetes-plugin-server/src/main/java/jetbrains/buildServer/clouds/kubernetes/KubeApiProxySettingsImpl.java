package jetbrains.buildServer.clouds.kubernetes;

import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiProxySettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class KubeApiProxySettingsImpl implements KubeApiProxySettings {
  @NotNull private final String myHttpProxy;
  @Nullable private final String myProxyUsername;
  @Nullable private final String myProxyPassword;
  @NotNull private final String[] myNonProxyHosts;

  public KubeApiProxySettingsImpl(@NotNull String httpProxy,
                                  @Nullable String proxyUsername,
                                  @Nullable String proxyPassword,
                                  @NotNull String[] nonProxyHosts) {
    myHttpProxy = httpProxy;
    myProxyUsername = proxyUsername;
    myProxyPassword = proxyPassword;
    myNonProxyHosts = nonProxyHosts;
  }


  @NotNull
  @Override
  public String getProxyUri() {
    return myHttpProxy;
  }

  @Nullable
  @Override
  public String getProxyUsername() {
    return myProxyUsername;
  }

  @Nullable
  @Override
  public String getProxyPassword() {
    return myProxyPassword;
  }

  @NotNull
  @Override
  public String[] getNonProxyHosts() {
    return myNonProxyHosts;
  }
}
