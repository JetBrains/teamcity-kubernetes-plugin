package jetbrains.buildServer.clouds.kubernetes;

import io.fabric8.kubernetes.api.model.Pod;
import java.util.Date;
import jetbrains.buildServer.clouds.CloudErrorInfo;
import jetbrains.buildServer.clouds.InstanceStatus;
import jetbrains.buildServer.serverSide.AgentDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StartingKubeCloudInstance implements KubeCloudInstance {

  private final KubeCloudImage myImage;
  private final String myInstanceId;
  private final Date myStartDate;

  public StartingKubeCloudInstance(final KubeCloudImage image, final String instanceId) {
    myImage = image;
    myInstanceId = instanceId;
    myStartDate = new Date();
  }

  @NotNull
  @Override
  public String getInstanceId() {
    return myInstanceId;
  }

  @NotNull
  @Override
  public String getName() {
    return myInstanceId;
  }

  @NotNull
  @Override
  public String getImageId() {
    return myImage.getId();
  }

  @NotNull
  @Override
  public KubeCloudImage getImage() {
    return myImage;
  }

  @NotNull
  @Override
  public Date getStartedTime() {
    return myStartDate;
  }

  @Nullable
  @Override
  public String getNetworkIdentity() {
    return null;
  }

  @NotNull
  @Override
  public InstanceStatus getStatus() {
    return InstanceStatus.SCHEDULED_TO_START;
  }

  @Nullable
  @Override
  public CloudErrorInfo getErrorInfo() {
    return null;
  }

  @Override
  public boolean containsAgent(@NotNull final AgentDescription agent) {
    return false;
  }

  @Override
  public void setStatus(final InstanceStatus status) {
    throw new UnsupportedOperationException("Cannot set status for starting instance");
  }

  @Override
  public void updateState(final Pod pod) {
    throw new UnsupportedOperationException("Cannot update state for starting instance");
  }

  @Override
  public void setError(@Nullable final CloudErrorInfo errorInfo) {
    throw new UnsupportedOperationException("Cannot set error for starting instance");
  }
}
