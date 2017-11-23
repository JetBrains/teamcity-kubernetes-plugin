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
