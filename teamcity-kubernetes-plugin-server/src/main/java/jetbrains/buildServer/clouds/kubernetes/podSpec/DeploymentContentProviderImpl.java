package jetbrains.buildServer.clouds.kubernetes.podSpec;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import jetbrains.buildServer.clouds.kubernetes.KubeCloudClientParameters;
import jetbrains.buildServer.clouds.kubernetes.auth.KubeAuthStrategyProvider;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnectorImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 04.09.17.
 */
public class DeploymentContentProviderImpl implements DeploymentContentProvider {
    private KubeAuthStrategyProvider myAuthStrategies;

    public DeploymentContentProviderImpl(KubeAuthStrategyProvider authStrategies) {
        myAuthStrategies = authStrategies;
    }

    @Nullable
    @Override
    public Deployment findDeployment(@NotNull String name, @NotNull KubeCloudClientParameters kubeClientParams) {
      KubeApiConnectorImpl kubeApiConnector = new KubeApiConnectorImpl("findDeployment", kubeClientParams, myAuthStrategies.get(kubeClientParams.getAuthStrategy()));
        //TODO:cache api call result
        return kubeApiConnector.getDeployment(name);
    }
}
