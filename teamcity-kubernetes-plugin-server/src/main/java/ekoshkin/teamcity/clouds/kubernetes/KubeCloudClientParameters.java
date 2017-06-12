package ekoshkin.teamcity.clouds.kubernetes;

import ekoshkin.teamcity.clouds.kubernetes.connector.KubeApiConnection;
import jetbrains.buildServer.clouds.CloudClientParameters;
import jetbrains.buildServer.clouds.CloudImageParameters;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.Converter;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static ekoshkin.teamcity.clouds.kubernetes.KubeParametersConstants.*;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 29.05.17.
 */
public class KubeCloudClientParameters implements KubeApiConnection {
    private static final String DEFAULT_NAMESPACE = "default";

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
        String explicitNameSpace = myParameters.getParameter(KUBERNETES_NAMESPACE);
        return StringUtil.isEmpty(explicitNameSpace) ? DEFAULT_NAMESPACE : explicitNameSpace;
    }

    public Collection<KubeCloudImageData> getImages(){
        return CollectionsUtil.convertCollection(myParameters.getCloudImages(), new Converter<KubeCloudImageData, CloudImageParameters>() {
            @Override
            public KubeCloudImageData createFrom(@NotNull CloudImageParameters cloudImageParameters) {
                return new KubeCloudImageData(cloudImageParameters);
            }
        });
    }
}
