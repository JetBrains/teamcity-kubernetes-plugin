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

import static jetbrains.buildServer.helm.HelmConstants.HELM_ROLLBACK_COMMAND_NAME;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 17.10.17.
 */
public class RollbackCommand implements HelmCommand {
    private final PluginDescriptor myPluginDescriptor;

    public RollbackCommand(PluginDescriptor pluginDescriptor, HelmCommandRegistry commandRegistry) {
        myPluginDescriptor = pluginDescriptor;
        commandRegistry.registerCommand(this);
    }

    @NotNull
    @Override
    public String getId() {
        return HELM_ROLLBACK_COMMAND_NAME;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Rollback";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Rolls back a release to a previous revision";
    }

    @Nullable
    @Override
    public PropertiesProcessor getPropertiesProcessor() {
        return properties -> {
            List<InvalidProperty> result = new Vector<InvalidProperty>();
            final String releaseName = properties.get(HelmConstants.RELEASE_NAME);
            if (PropertiesUtil.isEmptyOrNull(releaseName)) {
                result.add(new InvalidProperty(HelmConstants.RELEASE_NAME, "Release name must be specified"));
            }
            final Integer revision = PropertiesUtil.parseInt(properties.get(HelmConstants.REVISION));
            if (revision == null || revision <= 0) {
                result.add(new InvalidProperty(HelmConstants.REVISION, "Revision must be specified as positive integer"));
            }
            return result;
        };
    }

    @Nullable
    @Override
    public String getEditParamsJspFilePath() {
        return myPluginDescriptor.getPluginResourcesPath("helm/editRollback.jsp");
    }

    @Nullable
    @Override
    public String getViewParamsJspFilePath() {
        return myPluginDescriptor.getPluginResourcesPath("helm/viewRollback.jsp");
    }

    @NotNull
    @Override
    public String describeParameters(@NotNull Map<String, String> parameters) {
        String flags = parameters.get(HelmConstants.ADDITIONAL_FLAGS);
        return String.format("Release: %s\nRevision: %s\nAdditional flags: %s", parameters.get(HelmConstants.RELEASE_NAME), parameters.get(HelmConstants.REVISION), flags != null ? flags : "not specified");
    }
}
