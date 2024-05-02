
package jetbrains.buildServer.clouds.kubernetes;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.agent.impl.parameters.resolve.BuildRunnerEnvironmentPreprocessor;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static jetbrains.buildServer.clouds.kubernetes.KubeContainerEnvironment.TEAMCITY_KUBERNETES_PROVIDED_PREFIX;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 07.06.17.
 */
public class KubeAgentConfigurationProvider implements BuildRunnerEnvironmentPreprocessor {
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
                    LOG.warn("Could not find environment variable " + KubeContainerEnvironment.INSTANCE_NAME);
                }

                final String cloudInstanceHash = env.get(KubeContainerEnvironment.TEMPORARY_AUTH_TOKEN);
                if (StringUtil.isNotEmpty(cloudInstanceHash)) {
                    agentConfigurationEx.addConfigurationParameter(KubeContainerEnvironment.TEMPORARY_AUTHORIZATION_TOKEN_PARAM, cloudInstanceHash);
                } else {
                    LOG.warn("Could not find environment variable " + KubeContainerEnvironment.TEMPORARY_AUTH_TOKEN + ", the server may not be able to authorize this agent" );
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

    @Override
    public void preprocessBuildRunnerEnvironment(@NotNull BuildRunnerSettings buildRunnerSettings, @NotNull Map<String, String> map) {
        map.remove(KubeContainerEnvironment.TEMPORARY_AUTH_TOKEN);
    }
}