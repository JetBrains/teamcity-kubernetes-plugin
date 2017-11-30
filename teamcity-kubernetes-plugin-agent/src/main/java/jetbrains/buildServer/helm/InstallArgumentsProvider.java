package jetbrains.buildServer.helm;

import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import static jetbrains.buildServer.helm.HelmConstants.*;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 18.10.17.
 */
public class InstallArgumentsProvider implements HelmCommandArgumentsProvider {
    @NotNull
    @Override
    public String getCommandId() {
        return HELM_INSTALL_COMMAND_NAME;
    }

    @NotNull
    @Override
    public List<String> getArguments(@NotNull Map<String, String> runnerParameters) {
        String chart = runnerParameters.get(HELM_INSTALL_COMMAND_NAME + CHART);
        String additionalFlags = runnerParameters.get(HELM_INSTALL_COMMAND_NAME + ADDITIONAL_FLAGS);
        return StringUtil.splitCommandArgumentsAndUnquote(String.format("%s %s %s", HELM_INSTALL_COMMAND, StringUtil.emptyIfNull(chart), StringUtil.emptyIfNull(additionalFlags)));
    }
}
