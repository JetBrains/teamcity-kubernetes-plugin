package ekoshkin.teamcity.clouds.kubernetes;

import jetbrains.buildServer.clouds.CloudException;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 14.06.17.
 */
public class KubeCloudException extends CloudException {
    public KubeCloudException(String message) {
        super(message);
    }

    public KubeCloudException(String message, Throwable cause) {
        super(message, cause);
    }
}
