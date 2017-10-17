package jetbrains.buildServer.helm;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.runner.BuildServiceAdapter;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static jetbrains.buildServer.helm.HelmConstants.ADDITIONAL_FLAGS;
import static jetbrains.buildServer.helm.HelmConstants.CHART;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 18.10.17.
 */
public class InstallBuildService extends BuildServiceAdapter {
    @NotNull
    @Override
    public ProgramCommandLine makeProgramCommandLine() throws RunBuildException {
        final Map<String, String> runnerParameters = getRunnerParameters();
        return new ProgramCommandLine() {
            @NotNull
            @Override
            public String getExecutablePath() throws RunBuildException {
                return null;
            }

            @NotNull
            @Override
            public String getWorkingDirectory() throws RunBuildException {
                return null;
            }

            @NotNull
            @Override
            public List<String> getArguments() throws RunBuildException {
                return Arrays.asList("install", runnerParameters.get(CHART), runnerParameters.get(ADDITIONAL_FLAGS));
            }

            @NotNull
            @Override
            public Map<String, String> getEnvironment() throws RunBuildException {
                return null;
            }
        };
    }
}
