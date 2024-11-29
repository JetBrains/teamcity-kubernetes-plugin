
package jetbrains.buildServer.clouds.kubernetes.auth;

import io.fabric8.kubernetes.client.ConfigBuilder;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;
import jetbrains.buildServer.clouds.CloudConstants;
import jetbrains.buildServer.clouds.kubernetes.KubeCloudException;
import jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnection;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import jetbrains.buildServer.serverSide.oauth.OAuthConstants;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 14.06.17.
 */
public class DefaultServiceAccountAuthStrategy implements KubeAuthStrategy {
  private static final String DEFAULT_SERVICE_ACCOUNT_TOKEN_FILE = "/var/run/secrets/kubernetes.io/serviceaccount/token";
  public static final String TEAMCITY_KUBERNETES_LOCAL_SERVICE_ACCOUNT_ENABLE = "teamcity.kubernetes.localServiceAccount.enable";

  private final ProjectManager myProjectManager;

  public DefaultServiceAccountAuthStrategy(ProjectManager projectManager) {
    myProjectManager = projectManager;
  }

  @NotNull
    @Override
    public String getId() {
        return "service-account";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Default Service Account";
    }

    @Nullable
    @Override
    public String getDescription() {
        return null;
    }

    @NotNull
    @Override
    public ConfigBuilder apply(@NotNull ConfigBuilder clientConfig, @NotNull KubeApiConnection connection) {
        String defaultServiceAccountAuthToken = getDefaultServiceAccountAuthToken();
        if(StringUtil.isEmpty(defaultServiceAccountAuthToken)) throw new KubeCloudException("Can't locate default Kubernetes service account token.");
        return clientConfig.withOauthToken(defaultServiceAccountAuthToken);
    }

    @Override
    public boolean isAvailable(@Nullable String projectId) {
        final SProject project = myProjectManager.findProjectById(projectId);
        if (project == null) {
            return isEnabled();
        }

      return isEnabled() || isAuthStrategyUsed(project);
    }

  private boolean isAuthStrategyUsed(SProject project) {
    //TW-91057 This strategy is disabled by default but enabled for whoever was already using it
    return Stream.concat(
      project.getOwnFeaturesOfType(CloudConstants.CLOUD_PROFILE_FEATURE_TYPE).stream(),
      project.getOwnFeaturesOfType(OAuthConstants.FEATURE_TYPE).stream()
    ).anyMatch(features -> getId().equals(features.getParameters().get(KubeParametersConstants.AUTH_STRATEGY)));
  }

  private static boolean isEnabled() {
        return TeamCityProperties.getBoolean(TEAMCITY_KUBERNETES_LOCAL_SERVICE_ACCOUNT_ENABLE);
    }

    @Nullable
    private String getDefaultServiceAccountAuthToken() {
        try {
            return FileUtil.readText(new File(DEFAULT_SERVICE_ACCOUNT_TOKEN_FILE));
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public Collection<InvalidProperty> process(final Map<String, String> properties) {
        return null;
    }
}