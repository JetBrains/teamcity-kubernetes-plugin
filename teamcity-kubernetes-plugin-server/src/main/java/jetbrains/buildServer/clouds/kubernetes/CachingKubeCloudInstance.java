package jetbrains.buildServer.clouds.kubernetes;

import jetbrains.buildServer.clouds.CloudErrorInfo;
import jetbrains.buildServer.clouds.CloudImage;
import jetbrains.buildServer.clouds.InstanceStatus;
import jetbrains.buildServer.serverSide.AgentDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 24.10.17.
 */
public class CachingKubeCloudInstance implements KubeCloudInstance {
    private final KubeCloudInstance myInner;
    private final KubeDataCache myCache;

    CachingKubeCloudInstance(@NotNull KubeCloudInstance inner, @NotNull KubeDataCache cache) {
        myInner = inner;
        myCache = cache;
    }

    @Override
    public void terminate() {
        myInner.terminate();
        myCache.cleanInstanceStatus(myInner.getInstanceId());
    }

    @NotNull
    @Override
    public String getInstanceId() {
        return myInner.getInstanceId();
    }

    @NotNull
    @Override
    public String getName() {
        return myInner.getName();
    }

    @NotNull
    @Override
    public String getImageId() {
        return myInner.getImageId();
    }

    @NotNull
    @Override
    public CloudImage getImage() {
        return myInner.getImage();
    }

    @NotNull
    @Override
    public Date getStartedTime() {
        try {
            return myCache.getInstanceStartedTime(myInner.getInstanceId(), myInner::getStartedTime);
        } catch (Exception e) {
            return new Date();
        }
    }

    @Nullable
    @Override
    public String getNetworkIdentity() {
        return myInner.getNetworkIdentity();
    }

    @NotNull
    @Override
    public InstanceStatus getStatus() {
        try {
            return myCache.getInstanceStatus(myInner.getInstanceId(), myInner::getStatus);
        } catch (Exception e) {
            return InstanceStatus.UNKNOWN;
        }
    }

    @Nullable
    @Override
    public CloudErrorInfo getErrorInfo() {
        return myInner.getErrorInfo();
    }

    @Override
    public boolean containsAgent(@NotNull AgentDescription agentDescription) {
        return myInner.containsAgent(agentDescription);
    }
}
