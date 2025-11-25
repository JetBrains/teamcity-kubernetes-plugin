
package jetbrains.buildServer.clouds.kubernetes.podSpec;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import jetbrains.buildServer.clouds.kubernetes.KubeCloudClientParameters;
import jetbrains.buildServer.clouds.kubernetes.auth.KubeAuthStrategyProvider;
import jetbrains.buildServer.clouds.kubernetes.connection.KubernetesCredentialsFactory;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnectorImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 04.09.17.
 */
public class DeploymentContentProviderImpl implements DeploymentContentProvider {
    private final KubeAuthStrategyProvider myAuthStrategies;
    private final KubernetesCredentialsFactory myCredentialsFactory;
    public DeploymentContentProviderImpl(KubeAuthStrategyProvider authStrategies, KubernetesCredentialsFactory credentialsFactory) {
        myAuthStrategies = authStrategies;
      myCredentialsFactory = credentialsFactory;
    }

    @Nullable
    @Override
    public Deployment findDeployment(@NotNull String projectId, @NotNull String profileId, @NotNull String name, @NotNull KubeCloudClientParameters kubeClientParams) {
      try(        KubeApiConnectorImpl kubeApiConnector = new KubeApiConnectorImpl(profileId,
                                                                                   kubeClientParams,
                                                                                   myAuthStrategies.get(kubeClientParams.getAuthStrategy()),
                                                                                   myCredentialsFactory
      )
      ) {
        //TODO:cache api call result
        return kubeApiConnector.getDeployment(name);
      }
    }
}