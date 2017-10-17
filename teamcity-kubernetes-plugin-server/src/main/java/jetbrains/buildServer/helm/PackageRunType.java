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
public class PackageRunType extends RunType {
    private final PluginDescriptor myPluginDescriptor;

    public PackageRunType(PluginDescriptor pluginDescriptor, RunTypeRegistry runTypeRegistry) {
        myPluginDescriptor = pluginDescriptor;
        runTypeRegistry.registerRunType(this);
    }

    @NotNull
    @Override
    public String getType() {
        return "helm-package";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Helm Package";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Packages a chart into a versioned chart archive file.";
    }

    @Nullable
    @Override
    public PropertiesProcessor getRunnerPropertiesProcessor() {
        return null;
    }

    @Nullable
    @Override
    public String getEditRunnerParamsJspFilePath() {
        return myPluginDescriptor.getPluginResourcesPath("helm/editPackage.jsp");
    }

    @Nullable
    @Override
    public String getViewRunnerParamsJspFilePath() {
        return myPluginDescriptor.getPluginResourcesPath("helm/viewPAckage.jsp");
    }

    @Nullable
    @Override
    public Map<String, String> getDefaultRunnerProperties() {
        return null;
    }
}
