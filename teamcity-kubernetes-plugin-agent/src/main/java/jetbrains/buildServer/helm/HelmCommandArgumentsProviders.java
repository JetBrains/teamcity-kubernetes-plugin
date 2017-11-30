package jetbrains.buildServer.helm;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 29.11.17.
 */
public interface HelmCommandArgumentsProviders {
    @Nullable
    HelmCommandArgumentsProvider find(@NotNull String commandId);
}
