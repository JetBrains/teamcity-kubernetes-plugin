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

package jetbrains.buildServer.clouds.kubernetes;

import jetbrains.buildServer.clouds.CloudImageParameters;
import jetbrains.buildServer.clouds.kubernetes.connector.ImagePullPolicy;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants.AGENT_NAME_PREFIX;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 12.06.17.
 */
public class KubeCloudImageData {
    private final CloudImageParameters myRawImageData;

    public KubeCloudImageData(@NotNull final CloudImageParameters rawImageData) {
        myRawImageData = rawImageData;
    }

    public String getDockerImage() {
        return myRawImageData.getParameter(KubeParametersConstants.DOCKER_IMAGE);
    }

    @Nullable
    public ImagePullPolicy getImagePullPolicy() {
        final String parameter = myRawImageData.getParameter(KubeParametersConstants.IMAGE_PULL_POLICY);
        return StringUtil.isEmpty(parameter) ? null : ImagePullPolicy.valueOf(parameter);
    }

    public String getDockerArguments() {
        return myRawImageData.getParameter(KubeParametersConstants.DOCKER_ARGS);
    }

    public String getDockerCommand() {
        return myRawImageData.getParameter(KubeParametersConstants.DOCKER_CMD);
    }

    public String getId() {
        return KubeUtils.escapeForKube(myRawImageData.getId());
    }

    public Integer getAgentPoolId() {
        return myRawImageData.getAgentPoolId();
    }

    public String getPodSpecMode() {
        return myRawImageData.getParameter(KubeParametersConstants.POD_TEMPLATE_MODE);
    }

    public String getCustomPodTemplateContent() {
        return myRawImageData.getParameter(KubeParametersConstants.CUSTOM_POD_TEMPLATE);
    }

    public String getDeploymentName() {
        return myRawImageData.getParameter(KubeParametersConstants.SOURCE_DEPLOYMENT);
    }

    public int getInstanceLimit() {
        String parameter = myRawImageData.getParameter(KubeParametersConstants.IMAGE_INSTANCE_LIMIT);
        if(StringUtil.isEmpty(parameter)) return -1;
        return Integer.parseInt(parameter);
    }

    public String getAgentNamePrefix() {
        return myRawImageData.getParameter(AGENT_NAME_PREFIX);
    }

    public String getProfileId(){
        return myRawImageData.getProfileId();
    }
}
