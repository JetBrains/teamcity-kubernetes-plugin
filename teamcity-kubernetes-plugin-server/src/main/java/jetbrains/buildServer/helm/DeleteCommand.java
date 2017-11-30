package jetbrains.buildServer.helm;

import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.util.PropertiesUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import static jetbrains.buildServer.helm.HelmConstants.HELM_DELETE_COMMAND_NAME;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 17.10.17.
 */
public class DeleteCommand implements HelmCommand {
    @NotNull
    @Override
    public String getId() {
        return HELM_DELETE_COMMAND_NAME;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Delete";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Deletes the release from Kubernetes.";
    }

    @Nullable
    @Override
    public PropertiesProcessor getPropertiesProcessor() {
        return properties -> {
            List<InvalidProperty> result = new Vector<InvalidProperty>();
            final String chart = properties.get(HELM_DELETE_COMMAND_NAME + HelmConstants.RELEASE_NAME);
            if (PropertiesUtil.isEmptyOrNull(chart)) {
                result.add(new InvalidProperty(HELM_DELETE_COMMAND_NAME + HelmConstants.RELEASE_NAME, "Release name must be specified"));
            }
            return result;
        };
    }

    @Nullable
    @Override
    public String getEditParamsJspFile() {
        return "editDelete.jsp";
    }

    @Nullable
    @Override
    public String getViewParamsJspFile() {
        return "viewDelete.jsp";
    }

    @NotNull
    @Override
    public String describeParameters(@NotNull Map<String, String> parameters) {
        String flags = parameters.get(HELM_DELETE_COMMAND_NAME + HelmConstants.ADDITIONAL_FLAGS);
        return String.format("Release name: %s\nAdditional flags: %s", parameters.get(HELM_DELETE_COMMAND_NAME + HelmConstants.RELEASE_NAME), flags != null ? flags : "not specified");
    }
}