package jetbrains.buildServer.clouds.kubernetes;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 14.11.17.
 */
public interface KubeBackgroundUpdater {
    void registerClient(@NotNull KubeCloudClient client);
    void unregisterClient(@NotNull KubeCloudClient client);
}
