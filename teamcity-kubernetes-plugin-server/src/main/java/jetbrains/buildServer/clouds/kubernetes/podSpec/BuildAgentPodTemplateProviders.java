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

import jetbrains.buildServer.clouds.kubernetes.KubeCloudException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 15.06.17.
 */
public interface BuildAgentPodTemplateProviders {
    @NotNull
    Collection<BuildAgentPodTemplateProvider> getAll();

    @Nullable
    BuildAgentPodTemplateProvider find(@Nullable String id);

    @NotNull
    default BuildAgentPodTemplateProvider get(@Nullable String id){
        BuildAgentPodTemplateProvider podTemplateProvider = find(id);
        if(podTemplateProvider == null) throw new KubeCloudException("Unknown pod specification provider " + id);
        return podTemplateProvider;
    }
}
