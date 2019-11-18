package jetbrains.buildServer.clouds.kubernetes;

import io.fabric8.kubernetes.api.model.Pod;
import jetbrains.buildServer.clouds.CloudInstance;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 30.05.17.
 */
public interface KubeCloudInstance extends CloudInstance {
    void terminate();

    void updateState(Pod pod);
}
