
package jetbrains.buildServer.clouds.kubernetes.auth;

import com.google.common.annotations.VisibleForTesting;
import com.intellij.openapi.diagnostic.Logger;
import io.fabric8.kubernetes.client.ConfigBuilder;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
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
  public static final String DISABLED_MESSAGE = "The Default Service Account authentication strategy is disabled on this server.";

  private final ProjectManager myProjectManager;
  private final File myTokenFile;

  private final Logger LOG = Logger.getInstance(DefaultServiceAccountAuthStrategy.class);


  public DefaultServiceAccountAuthStrategy(ProjectManager projectManager) {
    this(projectManager, new File(DEFAULT_SERVICE_ACCOUNT_TOKEN_FILE));
  }

  @VisibleForTesting
  DefaultServiceAccountAuthStrategy(@NotNull ProjectManager projectManager, @NotNull File tokenFile) {
    myProjectManager = projectManager;
    myTokenFile = tokenFile;
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
        if (!isEnabled()) {
          LOG.info(DISABLED_MESSAGE + " To enable it, set the " + TEAMCITY_KUBERNETES_LOCAL_SERVICE_ACCOUNT_ENABLE + " internal property to true.");
          throw new KubeCloudException(DISABLED_MESSAGE + " Refer to teamcity-clouds.log for details.");
        }
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
    //TW-91057 This strategy is disabled by default. It stays in the list where a project already uses it,
    //so that the form shows the saved value and the error from process() instead of an empty choice
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
            return FileUtil.readText(myTokenFile);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public Collection<InvalidProperty> process(final Map<String, String> properties) {
        if (!isEnabled()) {
            return Collections.singletonList(new InvalidProperty(KubeParametersConstants.AUTH_STRATEGY, DISABLED_MESSAGE));
        }
        return Collections.emptyList();
    }
}