package jetbrains.buildServer.helm;

import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.util.PropertiesUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import static jetbrains.buildServer.helm.HelmConstants.HELM_UPGRADE_COMMAND_NAME;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 17.10.17.
 */
public class UpgradeCommand implements HelmCommand {
    @NotNull
    @Override
    public String getId() {
        return HELM_UPGRADE_COMMAND_NAME;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Upgrade";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Upgrades a release to a new version of a chart";
    }

    @Nullable
    @Override
    public PropertiesProcessor getPropertiesProcessor() {
        return properties -> {
            List<InvalidProperty> result = new Vector<InvalidProperty>();
            final String chart = properties.get(HELM_UPGRADE_COMMAND_NAME + HelmConstants.CHART);
            if (PropertiesUtil.isEmptyOrNull(chart)) {
                result.add(new InvalidProperty(HELM_UPGRADE_COMMAND_NAME + HelmConstants.CHART, "Chart must be specified"));
            }
            final String releaseName = properties.get(HELM_UPGRADE_COMMAND_NAME + HelmConstants.RELEASE_NAME);
            if (PropertiesUtil.isEmptyOrNull(releaseName)) {
                result.add(new InvalidProperty(HELM_UPGRADE_COMMAND_NAME + HelmConstants.RELEASE_NAME, "Release name must be specified"));
            }
            return result;
        };
    }

    @Nullable
    @Override
    public String getEditParamsJspFile() {
        return "editUpgrade.jsp";
    }

    @Nullable
    @Override
    public String getViewParamsJspFile() {
        return "viewUpgrade.jsp";
    }

    @NotNull
    @Override
    public String describeParameters(@NotNull Map<String, String> parameters) {
        String flags = parameters.get(HELM_UPGRADE_COMMAND_NAME + HelmConstants.ADDITIONAL_FLAGS);
        return String.format("Release: %s\nChart: %s\nAdditional flags: %s", parameters.get(HELM_UPGRADE_COMMAND_NAME + HelmConstants.RELEASE_NAME), parameters.get(HELM_UPGRADE_COMMAND_NAME + HelmConstants.CHART), flags != null ? flags : "not specified");
    }
}
