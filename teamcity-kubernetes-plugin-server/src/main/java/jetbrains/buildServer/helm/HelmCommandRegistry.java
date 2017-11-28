package jetbrains.buildServer.helm;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 28.11.17.
 */
public interface HelmCommandRegistry {
    void registerCommand(@NotNull HelmCommand helmCommand);
}
