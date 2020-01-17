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

import jetbrains.buildServer.agent.AgentLifeCycleAdapter;
import jetbrains.buildServer.agent.AgentLifeCycleListener;
import jetbrains.buildServer.agent.BuildAgent;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import static jetbrains.buildServer.helm.HelmConstants.HELM_PATH_CONFIG_PARAM;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 23.11.17.
 */
public class HelmAgentConfigurationProvider {
    public HelmAgentConfigurationProvider(@NotNull EventDispatcher<AgentLifeCycleListener> agentEvents) {
        agentEvents.addListener(new AgentLifeCycleAdapter(){
            @Override
            public void beforeAgentConfigurationLoaded(@NotNull BuildAgent agent) {
                File defaultHelmLocation = new File("/usr/local/bin/helm");
                if(defaultHelmLocation.exists()){
                    agent.getConfiguration().addConfigurationParameter(HELM_PATH_CONFIG_PARAM, defaultHelmLocation.getAbsolutePath());
                }
                super.beforeAgentConfigurationLoaded(agent);
            }
        });
    }
}
