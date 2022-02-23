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
import java.util.Collections;
import jetbrains.buildServer.clouds.CloudInstanceUserData;
import jetbrains.buildServer.clouds.kubernetes.*;
import jetbrains.buildServer.clouds.kubernetes.connector.ImagePullPolicy;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnector;
import jetbrains.buildServer.serverSide.ServerSettings;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 15.06.17.
 */
public class SimpleRunContainerProvider implements BuildAgentPodTemplateProvider {
    public static final String ID = "simple";
    private final ServerSettings myServerSettings;

    public SimpleRunContainerProvider(@NotNull ServerSettings serverSettings) {
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
        return "Run single container";
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

        ImagePullPolicy imagePullPolicy = kubeCloudImage.getImagePullPolicy();
        String serverAddress = cloudInstanceUserData.getServerAddress();
        String serverUUID = myServerSettings.getServerUUID();
        String cloudProfileId = cloudInstanceUserData.getProfileId();

        ContainerBuilder containerBuilder = new ContainerBuilder()
                .withName(instanceName)
                .withImage(kubeCloudImage.getDockerImage())
                .withImagePullPolicy(imagePullPolicy == null ? ImagePullPolicy.IfNotPresent.getName() : imagePullPolicy.getName())
                .withEnv(new EnvVar(KubeContainerEnvironment.SERVER_URL, serverAddress, null),
                         new EnvVar(KubeContainerEnvironment.SERVER_UUID, serverUUID, null),
                         new EnvVar(KubeContainerEnvironment.OFFICIAL_IMAGE_SERVER_URL, serverAddress, null),
                         new EnvVar(KubeContainerEnvironment.IMAGE_NAME, kubeCloudImage.getId(), null),
                         new EnvVar(KubeContainerEnvironment.PROFILE_ID, cloudProfileId, null),
                         new EnvVar(KubeContainerEnvironment.INSTANCE_NAME, instanceName, null));

        String dockerCommand = kubeCloudImage.getDockerCommand();
        if(!StringUtil.isEmpty(dockerCommand)) containerBuilder = containerBuilder.withCommand(dockerCommand);
        String dockerArguments = kubeCloudImage.getDockerArguments();
        if(!StringUtil.isEmpty(dockerArguments)) containerBuilder = containerBuilder.withArgs(dockerArguments);

        return new PodBuilder()
                .withNewMetadata()
                .withName(instanceName)
                .withNamespace(apiConnector.getNamespace())
                .withLabels(CollectionsUtil.asMap(
                        KubeTeamCityLabels.TEAMCITY_AGENT_LABEL, "",
                        KubeTeamCityLabels.TEAMCITY_SERVER_UUID, serverUUID,
                        KubeTeamCityLabels.TEAMCITY_CLOUD_PROFILE, cloudProfileId,
                        KubeTeamCityLabels.TEAMCITY_CLOUD_IMAGE, kubeCloudImage.getId()))
                .endMetadata()
                .withNewSpec()
                .withContainers(Collections.singletonList(containerBuilder.build()))
                .withRestartPolicy(KubeApiConnector.NEVER_RESTART_POLICY)
                .endSpec()
                .build();
    }

    @Nullable
    @Override
    public PersistentVolumeClaim getPVC(@NotNull final String instanceName, @NotNull final KubeCloudImage kubeCloudImage) {
        return null;
    }
}
