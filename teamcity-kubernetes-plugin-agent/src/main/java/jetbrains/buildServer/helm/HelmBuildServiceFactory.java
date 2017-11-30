package jetbrains.buildServer.helm;

import jetbrains.buildServer.agent.AgentBuildRunnerInfo;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.agent.runner.CommandLineBuildService;
import jetbrains.buildServer.agent.runner.CommandLineBuildServiceFactory;
import org.jetbrains.annotations.NotNull;

import static jetbrains.buildServer.helm.HelmConstants.HELM_PATH_CONFIG_PARAM;
import static jetbrains.buildServer.helm.HelmConstants.HELM_RUN_TYPE;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 29.11.17.
 */
public class HelmBuildServiceFactory implements CommandLineBuildServiceFactory {
    private final HelmCommandArgumentsProviders myCommandArgumentsProviders;

    public HelmBuildServiceFactory(HelmCommandArgumentsProviders commandArgumentsProviders) {
        myCommandArgumentsProviders = commandArgumentsProviders;
    }

    @NotNull
    @Override
    public CommandLineBuildService createService() {
        return new HelmBuildService(myCommandArgumentsProviders);
    }

    @NotNull
    @Override
    public AgentBuildRunnerInfo getBuildRunnerInfo() {
        return new AgentBuildRunnerInfo() {
            @NotNull
            @Override
            public String getType() {
                return HELM_RUN_TYPE;
            }

            @Override
            public boolean canRun(@NotNull BuildAgentConfiguration buildAgentConfiguration) {
                return buildAgentConfiguration.getConfigurationParameters().containsKey(HELM_PATH_CONFIG_PARAM);
            }
        };
    }
}
