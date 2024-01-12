
package jetbrains.buildServer.clouds.kubernetes;

import java.util.Collection;
import jetbrains.buildServer.clouds.CloudClientParameters;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import static jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants.*;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 29.05.17.
 */
public class KubeCloudClientParametersImpl extends ParametersKubeApiConnection implements KubeCloudClientParameters {

    @NotNull private final CloudClientParameters myCloudClientParameters;

    public KubeCloudClientParametersImpl(@NotNull CloudClientParameters parameters) {
        super(parameters.getParameters());
        myCloudClientParameters = parameters;
    }

    @NotNull
    public static KubeCloudClientParametersImpl create(@NotNull CloudClientParameters genericCloudClientParameters) {
        return new KubeCloudClientParametersImpl(genericCloudClientParameters);
    }

    @NotNull
    @Override
    public Collection<KubeCloudImageData> getImages(){
        return CollectionsUtil.convertCollection(myCloudClientParameters.getCloudImages(), KubeCloudImageData::new);
    }

    @Override
    public int getInstanceLimit() {
        String parameter = myParameters.get(PROFILE_INSTANCE_LIMIT);
        return StringUtil.isEmpty(parameter) ? -1 : Integer.valueOf(parameter);
    }
}