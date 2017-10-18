package jetbrains.buildServer.helm;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 17.10.17.
 */
public interface HelmConstants {
    String HELM_EXEC_NAME = "helm";

    String HELM_INSTALL_COMMAND = "install";
    String HELM_INSTALL_RUN_TYPE = "helm-install";

    String CHART = "teamcity.helm.chart";
    String ADDITIONAL_FLAGS = "teamcity.helm.additionalFlags";
}
