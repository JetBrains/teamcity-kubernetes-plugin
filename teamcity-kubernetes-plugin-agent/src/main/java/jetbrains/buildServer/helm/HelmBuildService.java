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

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.runner.BuildServiceAdapter;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import static jetbrains.buildServer.helm.HelmConstants.HELM_PATH_CONFIG_PARAM;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 29.11.17.
 */
public class HelmBuildService extends BuildServiceAdapter {
    private final HelmCommandArgumentsProviders myArgumentsProviders;

    HelmBuildService(HelmCommandArgumentsProviders argumentsProviders) {
        myArgumentsProviders = argumentsProviders;
    }

    @NotNull
    @Override
    public ProgramCommandLine makeProgramCommandLine() throws RunBuildException {
        return new ProgramCommandLine() {
            @NotNull
            @Override
            public String getExecutablePath() throws RunBuildException {
                return getConfigParameters().get(HELM_PATH_CONFIG_PARAM);
            }

            @NotNull
            @Override
            public String getWorkingDirectory() throws RunBuildException {
                return getRunnerContext().getBuild().getCheckoutDirectory().getAbsolutePath();
            }

            @NotNull
            @Override
            public List<String> getArguments() throws RunBuildException {
                Map<String, String> runnerParameters = getRunnerParameters();
                String commandId = runnerParameters.get(HelmConstants.COMMAND_ID);
                if(commandId == null){
                    throw new RunBuildException(HelmConstants.COMMAND_ID + " parameter value is null");
                }
                HelmCommandArgumentsProvider argumentsProvider = myArgumentsProviders.find(commandId);
                if (argumentsProvider == null) {
                    throw new RunBuildException("Can't find argumentsProvider for command with id " + commandId);
                }
                return argumentsProvider.getArguments(runnerParameters);
            }

            @NotNull
            @Override
            public Map<String, String> getEnvironment() throws RunBuildException {
                return getRunnerContext().getBuildParameters().getEnvironmentVariables();
            }
        };
    }
}
