package jetbrains.buildServer.helm;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 17.10.17.
 */
public interface HelmConstants {
    String HELM_PATH_CONFIG_PARAM = "Helm_Path";

    String HELM_INSTALL_COMMAND = "install";
    String HELM_INSTALL_RUN_TYPE = "helm-install";

    String HELM_DELETE_COMMAND = "delete";
    String HELM_DELETE_RUN_TYPE = "helm-delete";

    String HELM_UPGRADE_COMMAND = "upgrade";
    String HELM_UPGRADE_RUN_TYPE = "helm-upgrade";

    String HELM_ROLLBACK_COMMAND = "rollback";
    String HELM_ROLLBACK_RUN_TYPE = "helm-rollback";

    String HELM_TEST_COMMAND = "test";
    String HELM_TEST_RUN_TYPE = "helm-test";

    String CHART = "teamcity.helm.chart";
    String ADDITIONAL_FLAGS = "teamcity.helm.additionalFlags";
    String RELEASE_NAME = "teamcity.helm.releaseName";
    String REVISION = "teamcity.helm.revision";
}
