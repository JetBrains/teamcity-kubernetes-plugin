package ekoshkin.teamcity.clouds.kubernetes;

import jetbrains.buildServer.clouds.CloudInstance;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 30.05.17.
 */
public interface KubeCloudInstance extends CloudInstance {
    void terminate();
}
