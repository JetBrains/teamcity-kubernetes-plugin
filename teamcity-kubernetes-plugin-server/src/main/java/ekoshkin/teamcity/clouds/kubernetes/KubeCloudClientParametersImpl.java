package ekoshkin.teamcity.clouds.kubernetes;

import jetbrains.buildServer.clouds.CloudClientParameters;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import static ekoshkin.teamcity.clouds.kubernetes.KubeParametersConstants.*;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 29.05.17.
 */
public class KubeCloudClientParametersImpl implements KubeCloudClientParameters {
    private static final String DEFAULT_NAMESPACE = "default";

    private final CloudClientParameters myParameters;

    public KubeCloudClientParametersImpl(CloudClientParameters parameters) {
        myParameters = parameters;
    }

    @NotNull
    public static KubeCloudClientParametersImpl create(@NotNull CloudClientParameters genericCloudClientParameters) {
        return new KubeCloudClientParametersImpl(genericCloudClientParameters);
    }

    @NotNull
    @Override
    public String getApiServerUrl() {
        return myParameters.getParameter(API_SERVER_URL);
    }

    public String getNamespace(){
        String explicitNameSpace = myParameters.getParameter(KUBERNETES_NAMESPACE);
        return StringUtil.isEmpty(explicitNameSpace) ? DEFAULT_NAMESPACE : explicitNameSpace;
    }

    @Nullable
    @Override
    public String getCustomParameter(@NotNull String parameterName) {
        return myParameters.getParameter(parameterName);
    }

    @NotNull
    @Override
    public Collection<KubeCloudImageData> getImages(){
        return CollectionsUtil.convertCollection(myParameters.getCloudImages(), cloudImageParameters -> new KubeCloudImageData(cloudImageParameters));
    }

    @Override
    @NotNull
    public String getAuthStrategy() {
        return myParameters.getParameter(AUTH_STRATEGY);
    }
}
