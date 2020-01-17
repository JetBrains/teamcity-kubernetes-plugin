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

package jetbrains.buildServer.helm;

import jetbrains.buildServer.agent.AgentBuildRunnerInfo;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.agent.runner.CommandLineBuildService;
import jetbrains.buildServer.agent.runner.CommandLineBuildServiceFactory;
import org.jetbrains.annotations.NotNull;

import static jetbrains.buildServer.helm.HelmConstants.HELM_PATH_CONFIG_PARAM;
import static jetbrains.buildServer.helm.HelmConstants.HELM_RUN_TYPE;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 29.11.17.
 */
public class HelmBuildServiceFactory implements CommandLineBuildServiceFactory {
    private final HelmCommandArgumentsProviders myCommandArgumentsProviders;

    public HelmBuildServiceFactory(HelmCommandArgumentsProviders commandArgumentsProviders) {
        myCommandArgumentsProviders = commandArgumentsProviders;
    }

    @NotNull
    @Override
    public CommandLineBuildService createService() {
        return new HelmBuildService(myCommandArgumentsProviders);
    }

    @NotNull
    @Override
    public AgentBuildRunnerInfo getBuildRunnerInfo() {
        return new AgentBuildRunnerInfo() {
            @NotNull
            @Override
            public String getType() {
                return HELM_RUN_TYPE;
            }

            @Override
            public boolean canRun(@NotNull BuildAgentConfiguration buildAgentConfiguration) {
                return buildAgentConfiguration.getConfigurationParameters().containsKey(HELM_PATH_CONFIG_PARAM);
            }
        };
    }
}
