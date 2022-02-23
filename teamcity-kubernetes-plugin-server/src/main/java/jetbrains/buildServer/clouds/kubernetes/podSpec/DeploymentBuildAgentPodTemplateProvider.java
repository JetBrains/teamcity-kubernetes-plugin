/*
 * Copyright 2000-2022 JetBrains s.r.o.
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

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import jetbrains.buildServer.clouds.CloudInstanceUserData;
import jetbrains.buildServer.clouds.kubernetes.*;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnector;
import jetbrains.buildServer.serverSide.ServerSettings;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 15.06.17.
 */
public class DeploymentBuildAgentPodTemplateProvider extends AbstractPodTemplateProvider{
  public static final String ID = "deployment-base";

  private final ServerSettings myServerSettings;

  public DeploymentBuildAgentPodTemplateProvider(@NotNull ServerSettings serverSettings) {
    myServerSettings = serverSettings;
  }

  @NotNull
  @Override
  public String getId() {
    return ID;
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return "Use pod template from deployment";
  }

  @Nullable
  @Override
  public String getDescription() {
    return null;
  }

  @NotNull
  @Override
  public Pod getPodTemplate(@NotNull String instanceName,
                            @NotNull CloudInstanceUserData cloudInstanceUserData,
                            @NotNull KubeCloudImage kubeCloudImage,
                            @NotNull KubeApiConnector apiConnector) {
    String sourceDeploymentName = kubeCloudImage.getSourceDeploymentName();
    if (StringUtil.isEmpty(sourceDeploymentName)) {
      throw new KubeCloudException("Deployment name is not set in kubernetes cloud image " + kubeCloudImage.getId());
    }


    Deployment sourceDeployment = apiConnector.getDeployment(sourceDeploymentName);
    if (sourceDeployment == null) {
      throw new KubeCloudException("Can't find source deployment by name " + sourceDeploymentName);
    }

    //final String agentNameProvided = cloudInstanceUserData.getAgentName();
    //final String instanceName = StringUtil.isEmpty(agentNameProvided) ? sourceDeploymentName + "-" + UUID.randomUUID().toString() : agentNameProvided;

    return patchedPodTemplateSpec(sourceDeployment.getSpec().getTemplate(),
                                  instanceName,
                                  apiConnector.getNamespace(),
                                  myServerSettings.getServerUUID(),
                                  kubeCloudImage.getId(),
                                  cloudInstanceUserData
    );
    }
}
