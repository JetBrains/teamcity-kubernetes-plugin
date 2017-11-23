package jetbrains.buildServer.helm;

import jetbrains.buildServer.agent.AgentBuildRunnerInfo;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.agent.runner.CommandLineBuildService;
import jetbrains.buildServer.agent.runner.CommandLineBuildServiceFactory;
import org.jetbrains.annotations.NotNull;

import static jetbrains.buildServer.helm.HelmConstants.HELM_INSTALL_RUN_TYPE;
import static jetbrains.buildServer.helm.HelmConstants.HELM_PATH_CONFIG_PARAM;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 18.10.17.
 */
public class InstallBuildServiceFactory implements CommandLineBuildServiceFactory {
    @NotNull
    @Override
    public CommandLineBuildService createService() {
        return new InstallBuildService();
    }

    @NotNull
    @Override
    public AgentBuildRunnerInfo getBuildRunnerInfo() {
        return new AgentBuildRunnerInfo() {
            @NotNull
            @Override
            public String getType() {
                return HELM_INSTALL_RUN_TYPE;
            }

            @Override
            public boolean canRun(@NotNull BuildAgentConfiguration buildAgentConfiguration) {
                return buildAgentConfiguration.getConfigurationParameters().containsKey(HELM_PATH_CONFIG_PARAM);
            }
        };
    }
}
