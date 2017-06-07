package ekoshkin.teamcity.clouds.kubernetes;

import io.fabric8.kubernetes.api.model.Pod;
import jetbrains.buildServer.clouds.CloudInstance;
import org.jetbrains.annotations.NotNull;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 30.05.17.
 */
public interface KubeCloudInstance extends CloudInstance {
    @NotNull
    Pod getPod();
}
