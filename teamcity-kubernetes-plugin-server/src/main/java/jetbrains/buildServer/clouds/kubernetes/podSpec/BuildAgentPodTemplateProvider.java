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

import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Pod;
import jetbrains.buildServer.clouds.CloudInstanceUserData;
import jetbrains.buildServer.clouds.kubernetes.KubeCloudClientParameters;
import jetbrains.buildServer.clouds.kubernetes.KubeCloudImage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 15.06.17.
 */
public interface BuildAgentPodTemplateProvider {
    @NotNull String getId();
    @NotNull String getDisplayName();
    @Nullable String getDescription();

    @NotNull
    Pod getPodTemplate(@NotNull String instanceName,
                       @NotNull CloudInstanceUserData cloudInstanceUserData,
                       @NotNull KubeCloudImage kubeCloudImage,
                       @NotNull KubeCloudClientParameters clientParameters);

    @Nullable
    PersistentVolumeClaim getPVC(@NotNull String instanceName,
                                 @NotNull KubeCloudImage kubeCloudImage);
}
