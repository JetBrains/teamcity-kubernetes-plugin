package jetbrains.buildServer.helm;

import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.util.PropertiesUtil;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
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
    private final PluginDescriptor myPluginDescriptor;

    public UpgradeCommand(PluginDescriptor pluginDescriptor, HelmCommandRegistry commandRegistry) {
        myPluginDescriptor = pluginDescriptor;
        commandRegistry.registerCommand(this);
    }

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
            final String chart = properties.get(HelmConstants.CHART);
            if (PropertiesUtil.isEmptyOrNull(chart)) {
                result.add(new InvalidProperty(HelmConstants.CHART, "Chart must be specified"));
            }
            final String releaseName = properties.get(HelmConstants.RELEASE_NAME);
            if (PropertiesUtil.isEmptyOrNull(releaseName)) {
                result.add(new InvalidProperty(HelmConstants.RELEASE_NAME, "Release name must be specified"));
            }
            return result;
        };
    }

    @Nullable
    @Override
    public String getEditParamsJspFilePath() {
        return myPluginDescriptor.getPluginResourcesPath("helm/editUpgrade.jsp");
    }

    @Nullable
    @Override
    public String getViewParamsJspFilePath() {
        return myPluginDescriptor.getPluginResourcesPath("helm/viewUpgrade.jsp");
    }

    @NotNull
    @Override
    public String describeParameters(@NotNull Map<String, String> parameters) {
        String flags = parameters.get(HelmConstants.ADDITIONAL_FLAGS);
        return String.format("Release: %s\nChart: %s\nAdditional flags: %s", parameters.get(HelmConstants.RELEASE_NAME), parameters.get(HelmConstants.CHART), flags != null ? flags : "not specified");
    }
}
