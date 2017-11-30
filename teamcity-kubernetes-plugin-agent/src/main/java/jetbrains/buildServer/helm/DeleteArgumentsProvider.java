package jetbrains.buildServer.helm;

import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static jetbrains.buildServer.helm.HelmConstants.*;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 19.10.17.
 */
public class DeleteArgumentsProvider implements HelmCommandArgumentsProvider {
    @NotNull
    @Override
    public String getCommandId() {
        return HELM_DELETE_COMMAND_NAME;
    }

    @NotNull
    @Override
    public List<String> getArguments(@NotNull Map<String, String> runnerParameters) {
        List<String> result = new LinkedList<String>();
        result.add(HELM_DELETE_COMMAND);
        result.add(StringUtil.emptyIfNull(runnerParameters.get(HELM_DELETE_COMMAND_NAME + ADDITIONAL_FLAGS)));
        result.add(StringUtil.emptyIfNull(runnerParameters.get(HELM_DELETE_COMMAND_NAME + RELEASE_NAME)));
        return result;
    }
}
