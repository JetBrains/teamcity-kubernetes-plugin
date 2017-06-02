package ekoshkin.teamcity.clouds.kubernetes;

import ekoshkin.teamcity.clouds.kubernetes.connector.KubeApiConnector;
import ekoshkin.teamcity.clouds.kubernetes.connector.KubeApiConnectorImpl;
import ekoshkin.teamcity.clouds.kubernetes.web.KubeProfileEditController;
import jetbrains.buildServer.clouds.*;
import jetbrains.buildServer.serverSide.AgentDescription;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 27.05.17.
 */
public class KubeCloudClientFactory implements CloudClientFactory {

    public static final String DISPLAY_NAME = "Kubernetes";
    public static final String ID = "kubernetes";

    private final PluginDescriptor myPluginDescriptor;

    public KubeCloudClientFactory(@NotNull final CloudRegistrar registrar,
                                  @NotNull final PluginDescriptor pluginDescriptor) {
        myPluginDescriptor = pluginDescriptor;
        registrar.registerCloudFactory(this);
    }

    @NotNull
    @Override
    public String getCloudCode() {
        return ID;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Nullable
    @Override
    public String getEditProfileUrl() {
        return myPluginDescriptor.getPluginResourcesPath(KubeProfileEditController.EDIT_KUBE_HTML);
    }

    @NotNull
    @Override
    public Map<String, String> getInitialParameterValues() {
        return Collections.emptyMap();
    }

    @NotNull
    @Override
    public PropertiesProcessor getPropertiesProcessor() {
        return new KubeProfilePropertiesProcessor();
    }

    @Override
    public boolean canBeAgentOfType(@NotNull AgentDescription agentDescription) {
        return true;
    }

    @NotNull
    @Override
    public CloudClientEx createNewClient(@NotNull CloudState cloudState, @NotNull CloudClientParameters cloudClientParameters) {
        KubeCloudClientParameters kubeClientParams = KubeCloudClientParameters.create(cloudClientParameters);
        KubeApiConnector apiConnector = new KubeApiConnectorImpl();
        return new KubeCloudClient(apiConnector);
    }
}
