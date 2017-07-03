package jetbrains.buildServer.clouds.kubernetes;

import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnection;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 21.06.17.
 */
public interface KubeCloudClientParameters extends KubeApiConnection {
    @NotNull
    Collection<KubeCloudImageData> getImages();

    @NotNull
    String getAuthStrategy();
}
