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

import jetbrains.buildServer.serverSide.ServerSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 15.06.17.
 */
public class BuildAgentPodTemplateProvidersImpl implements BuildAgentPodTemplateProviders {
    private final Map<String, BuildAgentPodTemplateProvider> myIdToProviderMap = new HashMap<>();

    public BuildAgentPodTemplateProvidersImpl(@NotNull ServerSettings serverSettings,
                                              @NotNull DeploymentContentProvider deploymentContentProvider) {
        registerProvider(new SimpleRunContainerProvider(serverSettings));
        registerProvider(new DeploymentBuildAgentPodTemplateProvider(serverSettings));
        registerProvider(new CustomTemplatePodTemplateProvider(serverSettings));
    }

    @NotNull
    @Override
    public Collection<BuildAgentPodTemplateProvider> getAll() {
        return myIdToProviderMap.values();
    }

    @Nullable
    @Override
    public BuildAgentPodTemplateProvider find(@Nullable String id) {
        return myIdToProviderMap.get(id);
    }

    private void registerProvider(BuildAgentPodTemplateProvider podTemplateProvider) {
        myIdToProviderMap.put(podTemplateProvider.getId(), podTemplateProvider);
    }
}
