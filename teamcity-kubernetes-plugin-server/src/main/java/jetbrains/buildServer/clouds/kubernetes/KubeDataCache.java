package jetbrains.buildServer.clouds.kubernetes;

import jetbrains.buildServer.clouds.InstanceStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.concurrent.Callable;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 24.10.17.
 */
public interface KubeDataCache {
    @NotNull
    Date getInstanceStartedTime(@NotNull String instanceId, @NotNull Callable<Date> resolver) throws Exception;

    @NotNull
    InstanceStatus getInstanceStatus(@NotNull String instanceId, @NotNull Callable<InstanceStatus> resolver) throws Exception;
}
