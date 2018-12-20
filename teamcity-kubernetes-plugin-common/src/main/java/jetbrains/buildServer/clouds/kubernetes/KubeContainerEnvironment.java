package jetbrains.buildServer.clouds.kubernetes;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 07.06.17.
 */
public class KubeContainerEnvironment {
    public static final String TEAMCITY_KUBERNETES_PREFIX = "TEAMCITY_KUBERNETES_";
    public static final String TEAMCITY_KUBERNETES_PROVIDED_PREFIX = "TC_K8S_PROVIDED_";

    public static final String SERVER_URL = TEAMCITY_KUBERNETES_PREFIX + "SERVER_URL";
    public static final String SERVER_UUID = TEAMCITY_KUBERNETES_PREFIX + "SERVER_UUID";
    public static final String IMAGE_ID = TEAMCITY_KUBERNETES_PREFIX + "IMAGE_NAME";
    public static final String PROFILE_ID = TEAMCITY_KUBERNETES_PREFIX + "CLOUD_PROFILE_ID";
    public static final String INSTANCE_NAME = TEAMCITY_KUBERNETES_PREFIX + "INSTANCE_NAME";
    public static final String AGENT_NAME_PREFIX = TEAMCITY_KUBERNETES_PREFIX + "AGENT_NAME_PREFIX";

    public static final String OFFICIAL_IMAGE_SERVER_URL = "SERVER_URL";

    public static final String REQUIRED_PROFILE_ID_CONFIG_PARAM = "system.cloud.profile_id";
}
