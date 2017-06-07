package ekoshkin.teamcity.clouds.kubernetes;

import ekoshkin.teamcity.clouds.kubernetes.connector.KubeApiConnectionSettings;
import jetbrains.buildServer.clouds.CloudClientParameters;
import org.jetbrains.annotations.NotNull;

import static ekoshkin.teamcity.clouds.kubernetes.KubeConstants.*;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 29.05.17.
 */
public class KubeCloudClientParameters implements KubeApiConnectionSettings {
    private final CloudClientParameters myParameters;

    public KubeCloudClientParameters(CloudClientParameters parameters) {
        myParameters = parameters;
    }

    @NotNull
    public static KubeCloudClientParameters create(@NotNull CloudClientParameters genericCloudClientParameters) {
        return new KubeCloudClientParameters(genericCloudClientParameters);
    }

    @NotNull
    @Override
    public String getApiServerUrl() {
        return myParameters.getParameter(API_SERVER_URL);
    }

    @NotNull
    @Override
    public String getPassword() {
        return myParameters.getParameter(SERVICE_ACCOUNT_TOKEN);
    }

    @NotNull
    @Override
    public String getUsername() {
        return myParameters.getParameter(SERVICE_ACCOUNT_NAME);
    }

    public String getNamespace(){
        return myParameters.getParameter(KUBERNETES_NAMESPACE);
    }
}
