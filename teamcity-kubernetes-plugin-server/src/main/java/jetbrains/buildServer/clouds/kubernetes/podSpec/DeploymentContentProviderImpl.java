/*
 * Copyright 2000-2020 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
