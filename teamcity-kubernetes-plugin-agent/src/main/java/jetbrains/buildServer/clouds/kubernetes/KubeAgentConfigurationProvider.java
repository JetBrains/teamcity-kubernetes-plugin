
package jetbrains.buildServer.clouds.kubernetes;

import com.intellij.openapi.diagnostic.Logger;
import java.util.Map;
import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import static jetbrains.buildServer.clouds.kubernetes.KubeContainerEnvironment.*;

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
                    LOG.warn("Could not find environment variable " + KubeContainerEnvironment.INSTANCE_NAME);
                }

                final String cloudInstanceHash = env.get(KubeContainerEnvironment.STARTING_INSTANCE_ID);
                if (StringUtil.isNotEmpty(cloudInstanceHash)) {
                    agentConfigurationEx.addConfigurationParameter(STARTING_INSTANCE_ID_PARAM, cloudInstanceHash);
                } else {
                    LOG.warn("Could not find environment variable " + KubeContainerEnvironment.STARTING_INSTANCE_ID + ", the server may not be able to authorize this agent" );
                }

                for (Map.Entry<String, String> entry : env.entrySet()){
                    final String key = entry.getKey();
                    final String value = entry.getValue();
                    if (key.startsWith(TEAMCITY_KUBERNETES_PROVIDED_PREFIX)){
                        LOG.info("prop( " + key + ") : " + value);
                        final String extractedKey = key.substring(TEAMCITY_KUBERNETES_PROVIDED_PREFIX.length());
                        agentConfigurationEx.addConfigurationParameter(extractedKey, value);
                        agentConfigurationEx.addConfigurationParameter(envToParam(extractedKey), value);
                    }
                }
            }

            @Override
            public void buildStarted(@NotNull AgentRunningBuild runningBuild) {
                final String instanceId = runningBuild.getSharedBuildParameters().getEnvironmentVariables().get(KubeContainerEnvironment.STARTING_INSTANCE_ID);
                if (instanceId != null) {
                    // mark instance id as password to avoid showing it in plain text on the build parameters tab and in the build log
                    runningBuild.getPasswordReplacer().addPassword(instanceId);
                }
            }
        });
    }
}