package jetbrains.buildServer.helm;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 28.11.17.
 */
class HelmCommands {
    private static final List<HelmCommand> ourCommands = Arrays.asList(
            new DeleteCommand(),
            new InstallCommand(),
            new RollbackCommand(),
            new TestCommand(),
            new UpgradeCommand());

    @NotNull
    static Collection<HelmCommand> getAll() {
        return ourCommands;
    }

    @Nullable
    static HelmCommand find(String commandId) {
        return ourCommands.stream().filter(cmd -> cmd.getId().equals(commandId)).findAny().orElse(null);
    }
}
