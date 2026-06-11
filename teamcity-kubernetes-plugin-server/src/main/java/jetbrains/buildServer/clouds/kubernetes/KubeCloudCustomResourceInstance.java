
package jetbrains.buildServer.clouds.kubernetes;

import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.HasMetadata;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import jetbrains.buildServer.clouds.CloudErrorInfo;
import jetbrains.buildServer.clouds.InstanceStatus;
import jetbrains.buildServer.serverSide.AgentDescription;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static jetbrains.buildServer.clouds.kubernetes.KubeContainerEnvironment.INSTANCE_NAME;

/**
 * A cloud instance backed by an arbitrary custom resource (e.g. an XSmogVM Crossplane composite
 * reconciled into a KubeVirt VM). Unlike pods, custom resources have no universal status contract,
 * so the instance becomes RUNNING when either:
 * <ul>
 *   <li>the build agent baked into the VM image registers on the TeamCity server
 *       (see {@link KubeBackgroundUpdaterImpl}), or</li>
 *   <li>the resource reports the Crossplane-style condition {@code Ready=True}.</li>
 * </ul>
 * A {@code Synced=False} condition (Crossplane reconcile failure) is surfaced as the instance error.
 */
public class KubeCloudCustomResourceInstance implements KubeCloudInstance {
    static final String READY_CONDITION = "Ready";
    static final String SYNCED_CONDITION = "Synced";

    private final KubeCloudImage myKubeCloudImage;
    @NotNull private volatile GenericKubernetesResource myResource;
    private volatile InstanceStatus myInstanceStatus = InstanceStatus.SCHEDULED_TO_START;

    private volatile CloudErrorInfo myCurrentError;
    private final Date myCreationTime;
    private final SimpleDateFormat myCreationTimestampFormat;

    public KubeCloudCustomResourceInstance(@NotNull KubeCloudImage kubeCloudImage, @NotNull GenericKubernetesResource resource) {
        myKubeCloudImage = kubeCloudImage;
        myResource = resource;
        myCreationTime = new Date();
        myCreationTimestampFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        myCreationTimestampFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @NotNull
    @Override
    public String getInstanceId() {
        return myResource.getMetadata().getName();
    }

    @NotNull
    @Override
    public String getName() {
        return myResource.getMetadata().getName();
    }

    @NotNull
    @Override
    public String getImageId() {
        return myKubeCloudImage.getId();
    }

    @NotNull
    @Override
    public KubeCloudImage getImage() {
        return myKubeCloudImage;
    }

    @Override
    public void setStatus(final InstanceStatus status) {
        myInstanceStatus = status;
    }

    @NotNull
    @Override
    public Date getStartedTime() {
        final String creationTimestamp = myResource.getMetadata().getCreationTimestamp();
        if (!StringUtil.isEmpty(creationTimestamp)) {
            try {
                return myCreationTimestampFormat.parse(creationTimestamp);
            } catch (ParseException ignored) {
            }
        }
        return myCreationTime;
    }

    @Nullable
    @Override
    public String getNetworkIdentity() {
        return null;
    }

    @NotNull
    @Override
    public InstanceStatus getStatus() {
        return myInstanceStatus;
    }

    @Nullable
    @Override
    public CloudErrorInfo getErrorInfo() {
        return myCurrentError;
    }

    @Override
    public boolean containsAgent(@NotNull AgentDescription agentDescription) {
        final Map<String, String> buildParams = agentDescription.getBuildParameters();
        return getName().equals(buildParams.get("env." + INSTANCE_NAME));
    }

    @Override
    public void updateState(@NotNull HasMetadata resource) {
        if (!(resource instanceof GenericKubernetesResource)) {
            throw new KubeCloudException("Custom resource instance '" + getName() + "' cannot be updated from a " + resource.getKind());
        }
        myResource = (GenericKubernetesResource)resource;

        final Map<String, String> syncedCondition = findCondition(myResource, SYNCED_CONDITION);
        if (syncedCondition != null && "False".equalsIgnoreCase(syncedCondition.get("status"))) {
            final String reason = syncedCondition.get("reason");
            final String message = syncedCondition.get("message");
            myCurrentError = new CloudErrorInfo(StringUtil.notEmpty(reason, "ReconcileError"), StringUtil.emptyIfNull(message));
        } else {
            myCurrentError = null;
        }

        if (!getStatus().isCanTerminate()) { // don't update status if instance is going to be terminated
            return;
        }
        final Map<String, String> readyCondition = findCondition(myResource, READY_CONDITION);
        if (readyCondition != null && "True".equalsIgnoreCase(readyCondition.get("status"))) {
            setStatus(InstanceStatus.RUNNING);
        } else if (myInstanceStatus == InstanceStatus.SCHEDULED_TO_START) {
            // the resource exists on the API server but the VM is not ready yet
            setStatus(InstanceStatus.STARTING);
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private static Map<String, String> findCondition(@NotNull GenericKubernetesResource resource, @NotNull String conditionType) {
        final Map<String, Object> additionalProperties = resource.getAdditionalProperties();
        if (additionalProperties == null) return null;
        final Object status = additionalProperties.get("status");
        if (!(status instanceof Map)) return null;
        final Object conditions = ((Map<String, Object>)status).get("conditions");
        if (!(conditions instanceof List)) return null;
        for (Object condition : (List<Object>)conditions) {
            if (condition instanceof Map && conditionType.equals(((Map<String, Object>)condition).get("type"))) {
                return (Map<String, String>)(Map<?, ?>)condition;
            }
        }
        return null;
    }

    @Override
    public void setError(@Nullable final CloudErrorInfo errorInfo) {
        myCurrentError = errorInfo;
    }

    @Override
    @Nullable
    public String getPVCName() {
        return null;
    }
}
