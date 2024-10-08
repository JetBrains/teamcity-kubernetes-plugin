
package jetbrains.buildServer.clouds.kubernetes;

import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 07.06.17.
 */
public class KubeContainerEnvironment {
  public static final String TEAMCITY_KUBERNETES_PREFIX = "TC_K8S_";
  public static final String TEAMCITY_KUBERNETES_PROVIDED_PREFIX = "TC_K8S_PROVIDED_";

  public static final String SERVER_URL = TEAMCITY_KUBERNETES_PREFIX + "SERVER_URL";
  public static final String SERVER_UUID = TEAMCITY_KUBERNETES_PREFIX + "SERVER_UUID";
  public static final String IMAGE_NAME = TEAMCITY_KUBERNETES_PREFIX + "IMAGE_NAME";
  public static final String PROFILE_ID = TEAMCITY_KUBERNETES_PREFIX + "CLOUD_PROFILE_ID";
  public static final String STARTING_INSTANCE_ID = TEAMCITY_KUBERNETES_PREFIX + "STARTING_INSTANCE_ID";
  public static final String INSTANCE_NAME = TEAMCITY_KUBERNETES_PREFIX + "INSTANCE_NAME";
  public static final String BUILD_ID = TEAMCITY_KUBERNETES_PREFIX + "BUILD_ID";

  public static final String OFFICIAL_IMAGE_SERVER_URL = "SERVER_URL";

  public static final String REQUIRED_PROFILE_ID_CONFIG_PARAM = "system.cloud.profile_id";
  public static final String STARTING_INSTANCE_ID_PARAM = "teamcity.agent.startingInstanceId";
  private static final Pattern ENV_TO_PARAM_PATTERN = Pattern.compile("_");
  private static final Pattern PARAM_TO_ENV_PATTERN = Pattern.compile("[.]");

  @NotNull
  static public String paramToEnvVar(@NotNull String param) {
    return PARAM_TO_ENV_PATTERN.matcher(param).replaceAll("_");
  }

  @NotNull
  static public String envToParam(@NotNull String envVar) {
    return ENV_TO_PARAM_PATTERN.matcher(envVar).replaceAll(".");
  }
}