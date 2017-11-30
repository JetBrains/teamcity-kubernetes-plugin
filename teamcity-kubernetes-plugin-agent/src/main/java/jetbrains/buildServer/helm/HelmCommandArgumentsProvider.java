package jetbrains.buildServer.helm;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 29.11.17.
 */
public interface HelmCommandArgumentsProvider {
    @NotNull
    String getCommandId();

    @NotNull
    List<String> getArguments(@NotNull Map<String, String> runnerParameters);
}
