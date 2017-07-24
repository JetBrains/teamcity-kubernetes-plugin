package jetbrains.buildServer.clouds.kubernetes;

import jetbrains.buildServer.agent.AgentLifeCycleAdapter;
import jetbrains.buildServer.agent.AgentLifeCycleListener;
import jetbrains.buildServer.agent.BuildAgent;
import jetbrains.buildServer.agent.BuildAgentConfigurationEx;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 07.06.17.
 */
public class KubeAgentConfigurationProvider {
    @NotNull
    private final BuildAgentConfigurationEx myAgentConfigurationEx;

    public KubeAgentConfigurationProvider(@NotNull EventDispatcher<AgentLifeCycleListener> agentEvents,
                                          @NotNull BuildAgentConfigurationEx agentConfigurationEx) {
        myAgentConfigurationEx = agentConfigurationEx;
        agentEvents.addListener(new AgentLifeCycleAdapter(){
            @Override
            public void afterAgentConfigurationLoaded(@NotNull BuildAgent agent) {
                super.afterAgentConfigurationLoaded(agent);
                appendKubeSpecificConfiguration();
            }
        });
    }

    private void appendKubeSpecificConfiguration() {
        Map<String, String> env = System.getenv();

        String providedAgentName = env.get(KubeContainerEnvironment.AGENT_NAME);
        if(!StringUtil.isEmpty(providedAgentName)) myAgentConfigurationEx.setName(providedAgentName);
        String providedServerUrl = env.get(KubeContainerEnvironment.SERVER_URL);
        if(!StringUtil.isEmpty(providedServerUrl)) myAgentConfigurationEx.setServerUrl(providedServerUrl);

        myAgentConfigurationEx.addConfigurationParameter(KubeAgentProperties.IMAGE_NAME, env.get(KubeContainerEnvironment.IMAGE_NAME));
        myAgentConfigurationEx.addConfigurationParameter(KubeAgentProperties.INSTANCE_NAME, env.get(KubeContainerEnvironment.INSTANCE_NAME));
    }
}
