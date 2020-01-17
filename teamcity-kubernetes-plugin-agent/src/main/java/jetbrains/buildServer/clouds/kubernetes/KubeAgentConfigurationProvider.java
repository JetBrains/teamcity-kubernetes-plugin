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

package jetbrains.buildServer.clouds.kubernetes;

import com.intellij.openapi.diagnostic.Logger;
import java.io.File;
import java.io.IOException;
import jetbrains.buildServer.agent.AgentLifeCycleAdapter;
import jetbrains.buildServer.agent.AgentLifeCycleListener;
import jetbrains.buildServer.agent.BuildAgent;
import jetbrains.buildServer.agent.BuildAgentConfigurationEx;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import org.jetbrains.annotations.Nullable;

import static jetbrains.buildServer.clouds.kubernetes.KubeContainerEnvironment.TEAMCITY_KUBERNETES_PROVIDED_PREFIX;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 07.06.17.
 */
public class KubeAgentConfigurationProvider {
    private static final Logger LOG = Logger.getInstance(KubeAgentConfigurationProvider.class.getName());

    public KubeAgentConfigurationProvider(@NotNull EventDispatcher<AgentLifeCycleListener> agentEvents,
                                          @NotNull final BuildAgentConfigurationEx agentConfigurationEx) {
        LOG.info("Initializing Kube Provider...");
        agentEvents.addListener(new AgentLifeCycleAdapter(){
            @Override
            public void afterAgentConfigurationLoaded(@NotNull BuildAgent agent) {
                super.afterAgentConfigurationLoaded(agent);
                final Map<String, String> env = System.getenv();
                final String providedServerUrl = env.get(KubeContainerEnvironment.SERVER_URL);
                if(StringUtil.isNotEmpty(providedServerUrl)) {
                    LOG.info("Provided TeamCity Server URL: " + providedServerUrl);
                    agentConfigurationEx.setServerUrl(providedServerUrl);
                } else {
                    LOG.info("TeamCity Server URL was not provided. The instance wasn't started using TeamCity Kube integration.");
                    return;
                }

                final String profileId = env.get(KubeContainerEnvironment.PROFILE_ID);
                if(StringUtil.isNotEmpty(profileId)){
                    LOG.info("Provided Profile Id: " + profileId);
                    agentConfigurationEx.addConfigurationParameter(KubeContainerEnvironment.REQUIRED_PROFILE_ID_CONFIG_PARAM, profileId);
                } else {
                    LOG.info("Profile Id was not provided. The instance wasn't started using TeamCity Kube integration.");
                    return;
                }

                final String instanceName = env.get(KubeContainerEnvironment.INSTANCE_NAME);
                if (StringUtil.isNotEmpty(instanceName)) {
                    LOG.info("Setting instance name to " + instanceName);
                    agentConfigurationEx.setName(instanceName);
                } else {
                    LOG.warn("Couldn't find 'env." + KubeContainerEnvironment.INSTANCE_NAME + "' property" );
                }

                for (Map.Entry<String, String> entry : env.entrySet()){
                    final String key = entry.getKey();
                    final String value = entry.getValue();
                    if (key.startsWith(TEAMCITY_KUBERNETES_PROVIDED_PREFIX)){
                        LOG.info("prop( " + key + ") : " + value);
                        agentConfigurationEx.addConfigurationParameter(key.substring(TEAMCITY_KUBERNETES_PROVIDED_PREFIX.length()), value);
                    }
                }
            }
        });
    }

}
