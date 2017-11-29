package jetbrains.buildServer.helm;

import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.util.PropertiesUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import static jetbrains.buildServer.helm.HelmConstants.HELM_TEST_COMMAND_NAME;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 17.10.17.
 */
public class TestCommand implements HelmCommand {
    @NotNull
    @Override
    public String getId() {
        return HELM_TEST_COMMAND_NAME;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Test";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Runs the tests for a release.";
    }

    @Nullable
    @Override
    public PropertiesProcessor getPropertiesProcessor() {
        return properties -> {
            List<InvalidProperty> result = new Vector<InvalidProperty>();
            final String chart = properties.get(HELM_TEST_COMMAND_NAME + HelmConstants.RELEASE_NAME);
            if (PropertiesUtil.isEmptyOrNull(chart)) {
                result.add(new InvalidProperty(HELM_TEST_COMMAND_NAME + HelmConstants.RELEASE_NAME, "Release name must be specified"));
            }
            return result;
        };
    }

    @Nullable
    @Override
    public String getEditParamsJspFile() {
        return "editTest.jsp";
    }

    @Nullable
    @Override
    public String getViewParamsJspFile() {
        return "viewTest.jsp";
    }

    @NotNull
    @Override
    public String describeParameters(@NotNull Map<String, String> parameters) {
        String flags = parameters.get(HELM_TEST_COMMAND_NAME + HelmConstants.ADDITIONAL_FLAGS);
        return String.format("Release name: %s\nAdditional flags: %s", parameters.get(HELM_TEST_COMMAND_NAME + HelmConstants.RELEASE_NAME), flags != null ? flags : "not specified");
    }
}
