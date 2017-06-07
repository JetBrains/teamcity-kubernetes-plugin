package ekoshkin.teamcity.clouds.kubernetes;

import ekoshkin.teamcity.clouds.kubernetes.connector.KubeApiConnector;
import jetbrains.buildServer.clouds.CloudErrorInfo;
import jetbrains.buildServer.clouds.CloudInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 07.06.17.
 */
public class KubeCloudImageImpl implements KubeCloudImage {
    private final KubeApiConnector myApiConnector;

    public KubeCloudImageImpl(@NotNull final KubeApiConnector apiConnector) {
        myApiConnector = apiConnector;
    }

    @NotNull
    @Override
    public String getContainerImage() {
        return null;
    }

    @Override
    public boolean isAlwaysPullImage() {
        return false;
    }

    @Nullable
    @Override
    public String getContainerArguments() {
        return null;
    }

    @Nullable
    @Override
    public String getContainerCommand() {
        return null;
    }

    @NotNull
    @Override
    public String getId() {
        return null;
    }

    @NotNull
    @Override
    public String getName() {
        return null;
    }

    @NotNull
    @Override
    public Collection<? extends CloudInstance> getInstances() {
        return null;
    }

    @Nullable
    @Override
    public CloudInstance findInstanceById(@NotNull String s) {
        return null;
    }

    @Nullable
    @Override
    public Integer getAgentPoolId() {
        return null;
    }

    @Nullable
    @Override
    public CloudErrorInfo getErrorInfo() {
        return null;
    }
}
