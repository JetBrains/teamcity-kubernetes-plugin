package jetbrains.buildServer.helm;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.runner.BuildServiceAdapter;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import static jetbrains.buildServer.helm.HelmConstants.HELM_PATH_CONFIG_PARAM;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 29.11.17.
 */
public class HelmBuildService extends BuildServiceAdapter {
    private final HelmCommandArgumentsProviders myArgumentsProviders;

    HelmBuildService(HelmCommandArgumentsProviders argumentsProviders) {
        myArgumentsProviders = argumentsProviders;
    }

    @NotNull
    @Override
    public ProgramCommandLine makeProgramCommandLine() throws RunBuildException {
        return new ProgramCommandLine() {
            @NotNull
            @Override
            public String getExecutablePath() throws RunBuildException {
                return getConfigParameters().get(HELM_PATH_CONFIG_PARAM);
            }

            @NotNull
            @Override
            public String getWorkingDirectory() throws RunBuildException {
                return getRunnerContext().getBuild().getCheckoutDirectory().getAbsolutePath();
            }

            @NotNull
            @Override
            public List<String> getArguments() throws RunBuildException {
                Map<String, String> runnerParameters = getRunnerParameters();
                String commandId = runnerParameters.get(HelmConstants.COMMAND_ID);
                if(commandId == null){
                    throw new RunBuildException(HelmConstants.COMMAND_ID + " parameter value is null");
                }
                HelmCommandArgumentsProvider argumentsProvider = myArgumentsProviders.find(commandId);
                if (argumentsProvider == null) {
                    throw new RunBuildException("Can't find argumentsProvider for command with id " + commandId);
                }
                return argumentsProvider.getArguments(runnerParameters);
            }

            @NotNull
            @Override
            public Map<String, String> getEnvironment() throws RunBuildException {
                return getRunnerContext().getBuildParameters().getEnvironmentVariables();
            }
        };
    }
}
