package ekoshkin.teamcity.clouds.kubernetes.connector;

import ekoshkin.teamcity.clouds.kubernetes.KubeCloudClientParameters;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 28.05.17.
 */
public class KubeApiConnectorImpl implements KubeApiConnector {
    private final KubeCloudClientParameters myKubeClientParams;

    public KubeApiConnectorImpl(KubeCloudClientParameters kubeClientParams) {
        myKubeClientParams = kubeClientParams;
    }

    @NotNull
    @Override
    public Deployment createDeployment(String deploymentName, String image) throws KubeApiException {
        return null;
    }

    @NotNull
    @Override
    public Deployment patchDeployment(String deploymentName, KubeObjectPatch patch) throws KubeApiException {
        return null;
    }

    @Nullable
    @Override
    public Deployment findDeployment(@NotNull String name) {
        return null;
    }

    @Override
    public boolean deleteDeployment(String name) {
        return false;
    }

    @Override
    public boolean deletePod(String name) {
        return false;
    }
}
