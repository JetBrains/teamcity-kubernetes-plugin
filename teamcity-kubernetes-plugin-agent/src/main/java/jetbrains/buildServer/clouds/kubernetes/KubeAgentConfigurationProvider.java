package jetbrains.buildServer.clouds.kubernetes;

import jetbrains.buildServer.agent.AgentLifeCycleAdapter;
import jetbrains.buildServer.agent.AgentLifeCycleListener;
import jetbrains.buildServer.agent.BuildAgent;
import jetbrains.buildServer.agent.BuildAgentConfigurationEx;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static jetbrains.buildServer.clouds.kubernetes.KubeContainerEnvironment.TEAMCITY_KUBERNETES_PROVIDED_PREFIX;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 07.06.17.
 */
public class KubeAgentConfigurationProvider {
    public KubeAgentConfigurationProvider(@NotNull EventDispatcher<AgentLifeCycleListener> agentEvents,
                                          @NotNull final BuildAgentConfigurationEx agentConfigurationEx) {
        agentEvents.addListener(new AgentLifeCycleAdapter(){
            @Override
            public void afterAgentConfigurationLoaded(@NotNull BuildAgent agent) {
                super.afterAgentConfigurationLoaded(agent);
                final Map<String, String> env = System.getenv();
                final String providedServerUrl = env.get(KubeContainerEnvironment.SERVER_URL);
                if(!StringUtil.isEmpty(providedServerUrl)) agentConfigurationEx.setServerUrl(providedServerUrl);

                final String profileId = env.get(KubeContainerEnvironment.PROFILE_ID);
                if(!StringUtil.isEmpty(providedServerUrl)) agentConfigurationEx.addConfigurationParameter(KubeContainerEnvironment.REQUIRED_PROFILE_ID_CONFIG_PARAM, profileId);

                for (Map.Entry<String, String> entry : env.entrySet()){
                    final String key = entry.getKey();
                    final String value = entry.getValue();
                    if (key.startsWith(TEAMCITY_KUBERNETES_PROVIDED_PREFIX)){
                        agentConfigurationEx.addConfigurationParameter(key.substring(TEAMCITY_KUBERNETES_PROVIDED_PREFIX.length()), value);
                    }
                }
            }
        });
    }
}
