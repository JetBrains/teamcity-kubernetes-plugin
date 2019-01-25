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
                    updateAgentNameIfNeeded(agentConfigurationEx, instanceName);
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


    public void updateAgentNameIfNeeded(@NotNull BuildAgentConfigurationEx conf,
                                        @Nullable String containerName){
        final File file = new File(conf.getAgentConfDirectory(), "k8s-"+containerName);
        if (file.exists()) {
            LOG.info(String.format("Marker file '%s' exists. Agent name will not be updated", file.getName()));
            return;
        }
        LOG.info(String.format("Marker file '%s' doesn't exist. Agent name will be updated", file.getName()));
        conf.setName(containerName);
        try {
            file.createNewFile();
        } catch (IOException e) {
            LOG.warnAndDebugDetails("Unable to create a marker file " + file.getName(), e);
        }
    }
}
