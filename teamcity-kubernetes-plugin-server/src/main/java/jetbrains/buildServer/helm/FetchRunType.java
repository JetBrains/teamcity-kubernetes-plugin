package jetbrains.buildServer.helm;

import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.RunType;
import jetbrains.buildServer.serverSide.RunTypeRegistry;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 17.10.17.
 */
public class FetchRunType extends RunType {
    private final PluginDescriptor myPluginDescriptor;

    public FetchRunType(PluginDescriptor pluginDescriptor, RunTypeRegistry runTypeRegistry) {
        myPluginDescriptor = pluginDescriptor;
        runTypeRegistry.registerRunType(this);
    }

    @NotNull
    @Override
    public String getType() {
        return "helm-fetch";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Helm Fetch";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Retrieve a package from a package repository, and download it locally.";
    }

    @Nullable
    @Override
    public PropertiesProcessor getRunnerPropertiesProcessor() {
        return null;
    }

    @Nullable
    @Override
    public String getEditRunnerParamsJspFilePath() {
        return myPluginDescriptor.getPluginResourcesPath("helm/editFetch.jsp");
    }

    @Nullable
    @Override
    public String getViewRunnerParamsJspFilePath() {
        return myPluginDescriptor.getPluginResourcesPath("helm/viewFetch.jsp");
    }

    @Nullable
    @Override
    public Map<String, String> getDefaultRunnerProperties() {
        return null;
    }
}
