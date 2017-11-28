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

import static jetbrains.buildServer.helm.HelmConstants.HELM_INSTALL_COMMAND_NAME;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 17.10.17.
 */
public class InstallCommand implements HelmCommand {
    private final PluginDescriptor myPluginDescriptor;

    public InstallCommand(PluginDescriptor pluginDescriptor, HelmCommandRegistry commandRegistry) {
        myPluginDescriptor = pluginDescriptor;
        commandRegistry.registerCommand(this);
    }

    @NotNull
    @Override
    public String getId() {
        return HELM_INSTALL_COMMAND_NAME;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Helm Install";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Installs Helm chart archive";
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
            return result;
        };
    }

    @Nullable
    @Override
    public String getEditParamsJspFilePath() {
        return myPluginDescriptor.getPluginResourcesPath("helm/editInstall.jsp");
    }

    @Nullable
    @Override
    public String getViewParamsJspFilePath() {
        return myPluginDescriptor.getPluginResourcesPath("helm/viewInstall.jsp");
    }

    @NotNull
    @Override
    public String describeParameters(@NotNull Map<String, String> parameters) {
        String flags = parameters.get(HelmConstants.ADDITIONAL_FLAGS);
        return String.format("Chart: %s\nAdditional flags: %s", parameters.get(HelmConstants.CHART), flags != null ? flags : "not specified");
    }
}
