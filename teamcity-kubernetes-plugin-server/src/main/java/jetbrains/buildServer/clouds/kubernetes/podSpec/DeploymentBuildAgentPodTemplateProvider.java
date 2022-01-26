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

import com.intellij.openapi.util.Pair;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import java.io.File;
import jetbrains.buildServer.clouds.CloudInstanceUserData;
import jetbrains.buildServer.clouds.kubernetes.*;
import jetbrains.buildServer.serverSide.ServerSettings;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 15.06.17.
 */
public class DeploymentBuildAgentPodTemplateProvider extends AbstractPodTemplateProvider{
  public static final String ID = "deployment-base";

  private final ServerSettings myServerSettings;
  private final DeploymentContentProvider myDeploymentContentProvider;
  private KubePodNameGenerator myPodNameGenerator;

  public DeploymentBuildAgentPodTemplateProvider(@NotNull ServerSettings serverSettings,
                                                 @NotNull DeploymentContentProvider deploymentContentProvider) {
    myServerSettings = serverSettings;
    myDeploymentContentProvider = deploymentContentProvider;
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
                            @NotNull KubeCloudClientParameters kubeClientParams) {
    String sourceDeploymentName = kubeCloudImage.getSourceDeploymentName();
    if (StringUtil.isEmpty(sourceDeploymentName)) {
      throw new KubeCloudException("Deployment name is not set in kubernetes cloud image " + kubeCloudImage.getId());
    }

    Deployment sourceDeployment = myDeploymentContentProvider.findDeployment(sourceDeploymentName, kubeClientParams);
    if (sourceDeployment == null) {
      throw new KubeCloudException("Can't find source deployment by name " + sourceDeploymentName);
    }

    //final String agentNameProvided = cloudInstanceUserData.getAgentName();
    //final String instanceName = StringUtil.isEmpty(agentNameProvided) ? sourceDeploymentName + "-" + UUID.randomUUID().toString() : agentNameProvided;

    return patchedPodTemplateSpec(sourceDeployment.getSpec().getTemplate(),
                                  instanceName,
                                  kubeClientParams.getNamespace(),
                                  myServerSettings.getServerUUID(),
                                  kubeCloudImage.getId(),
                                  cloudInstanceUserData
    );
    }
}
