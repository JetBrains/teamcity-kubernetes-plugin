package jetbrains.buildServer.clouds.kubernetes;

import org.jetbrains.annotations.NotNull;

public interface KubePodNameGenerator {

  @NotNull String generateNewVmName(@NotNull KubeCloudImage image);
}
