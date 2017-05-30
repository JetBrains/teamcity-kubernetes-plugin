package ekoshkin.teamcity.clouds.kubernetes;

import jetbrains.buildServer.clouds.CloudClientParameters;
import org.jetbrains.annotations.NotNull;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 29.05.17.
 */
public class KubeCloudClientParameters {
    private final CloudClientParameters myParameters;

    public KubeCloudClientParameters(CloudClientParameters parameters) {
        myParameters = parameters;
    }

    @NotNull
    public static KubeCloudClientParameters create(@NotNull CloudClientParameters genericCloudClientParameters) {
        return new KubeCloudClientParameters(genericCloudClientParameters);
    }
}
