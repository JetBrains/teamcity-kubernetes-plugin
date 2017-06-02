package ekoshkin.teamcity.clouds.kubernetes.connector;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 28.05.17.
 */
public interface KubeApiConnector {
    @NotNull
    Deployment createDeployment(String deploymentName, String image) throws KubeApiException;

    @NotNull
    Deployment patchDeployment(String deploymentName, KubeObjectPatch patch) throws KubeApiException;

    @Nullable
    Deployment findDeployment(@NotNull String name);

    boolean deleteDeployment(String name);

    boolean deletePod(String name);

    @NotNull
    KubeApiConnectionCheckResult testConnection(@NotNull KubeApiConnectionSettings connectionSettings);
}
