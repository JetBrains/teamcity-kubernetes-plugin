package jetbrains.buildServer.helm;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 17.10.17.
 */
public interface HelmConstants {
    String HELM_RUN_TYPE = "jetbrains.helm";

    String HELM_PATH_CONFIG_PARAM = "Helm_Path";

    String HELM_INSTALL_COMMAND = "install";
    String HELM_INSTALL_COMMAND_NAME = "helm-install";

    String HELM_DELETE_COMMAND = "delete";
    String HELM_DELETE_COMMAND_NAME = "helm-delete";

    String HELM_UPGRADE_COMMAND = "upgrade";
    String HELM_UPGRADE_COMMAND_NAME = "helm-upgrade";

    String HELM_ROLLBACK_COMMAND = "rollback";
    String HELM_ROLLBACK_COMMAND_NAME = "helm-rollback";

    String HELM_TEST_COMMAND = "test";
    String HELM_TEST_COMMAND_NAME = "helm-test";

    String CHART = "teamcity.helm.chart";
    String ADDITIONAL_FLAGS = "teamcity.helm.additionalFlags";
    String RELEASE_NAME = "teamcity.helm.releaseName";
    String REVISION = "teamcity.helm.revision";
    String COMMAND_ID = "teamcity.helm.command";
}
