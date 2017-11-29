package jetbrains.buildServer.helm;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 17.10.17.
 */
public class HelmConstantsBean {
    public String getChartKey() {
        return HelmConstants.CHART;
    }

    public String getReleaseName() {
        return HelmConstants.RELEASE_NAME;
    }

    public String getRevision() {
        return HelmConstants.REVISION;
    }

    public String getAdditionalFlagsKey() {
        return HelmConstants.ADDITIONAL_FLAGS;
    }

    public String getDeleteCommandId() {
        return HelmConstants.HELM_DELETE_COMMAND_NAME;
    }

    public String getInstallCommandId() {
        return HelmConstants.HELM_INSTALL_COMMAND_NAME;
    }

    public String getRollbackCommandId() {
        return HelmConstants.HELM_ROLLBACK_COMMAND_NAME;
    }

    public String getTestCommandId() {
        return HelmConstants.HELM_TEST_COMMAND_NAME;
    }

    public String getUpgradeCommandId() {
        return HelmConstants.HELM_UPGRADE_COMMAND_NAME;
    }
}
