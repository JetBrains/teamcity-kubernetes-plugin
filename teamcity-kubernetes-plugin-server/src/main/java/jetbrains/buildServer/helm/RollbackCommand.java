package jetbrains.buildServer.helm;

import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.util.PropertiesUtil;
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
            final String releaseName = properties.get(HELM_ROLLBACK_COMMAND_NAME + HelmConstants.RELEASE_NAME);
            if (PropertiesUtil.isEmptyOrNull(releaseName)) {
                result.add(new InvalidProperty(HELM_ROLLBACK_COMMAND_NAME + HelmConstants.RELEASE_NAME, "Release name must be specified"));
            }
            final Integer revision = PropertiesUtil.parseInt(properties.get(HELM_ROLLBACK_COMMAND_NAME + HelmConstants.REVISION));
            if (revision == null || revision <= 0) {
                result.add(new InvalidProperty(HELM_ROLLBACK_COMMAND_NAME + HelmConstants.REVISION, "Revision must be specified as positive integer"));
            }
            return result;
        };
    }

    @Nullable
    @Override
    public String getEditParamsJspFile() {
        return "editRollback.jsp";
    }

    @Nullable
    @Override
    public String getViewParamsJspFile() {
        return "viewRollback.jsp";
    }

    @NotNull
    @Override
    public String describeParameters(@NotNull Map<String, String> parameters) {
        String flags = parameters.get(HELM_ROLLBACK_COMMAND_NAME + HelmConstants.ADDITIONAL_FLAGS);
        return String.format("Release: %s\nRevision: %s\nAdditional flags: %s", parameters.get(HELM_ROLLBACK_COMMAND_NAME + HelmConstants.RELEASE_NAME), parameters.get(HELM_ROLLBACK_COMMAND_NAME + HelmConstants.REVISION), flags != null ? flags : "not specified");
    }
}
