
package jetbrains.buildServer.clouds.kubernetes.podSpec;

import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.utils.Serialization;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import jetbrains.buildServer.clouds.CloudInstanceUserData;
import jetbrains.buildServer.clouds.kubernetes.KubeCloudException;
import jetbrains.buildServer.clouds.kubernetes.KubeCloudImage;
import jetbrains.buildServer.clouds.kubernetes.KubeTeamCityLabels;
import jetbrains.buildServer.clouds.kubernetes.connector.CustomResourceContext;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnector;
import jetbrains.buildServer.serverSide.ServerSettings;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Deploy mode that provisions build agents from a user-supplied custom resource manifest
 * (e.g. an XSmogVM Crossplane composite in a KCP workspace) instead of a Pod.
 *
 * The manifest supports the following placeholders, which the user is expected to wire into
 * the VM bootstrap (cloud-init / sysprep) so the agent baked into the golden image
 * self-registers with the matching TeamCity environment variables:
 * <ul>
 *   <li>{@code %instance.id%} - generated instance/resource name (must equal env TC_K8S_INSTANCE_NAME)</li>
 *   <li>{@code %agent.name%}  - agent name (name prefix + instance id)</li>
 *   <li>{@code %server.url%}  - TeamCity server URL</li>
 *   <li>{@code %server.uuid%} - TeamCity server UUID (env TC_K8S_SERVER_UUID)</li>
 *   <li>{@code %profile.id%}  - cloud profile id (env TC_K8S_CLOUD_PROFILE_ID)</li>
 *   <li>{@code %image.id%}    - cloud image id (env TC_K8S_IMAGE_NAME)</li>
 * </ul>
 */
public class CustomResourceTemplateProvider implements BuildAgentPodTemplateProvider {
    public static final String ID = "custom-resource";

    private final ServerSettings myServerSettings;

    public CustomResourceTemplateProvider(ServerSettings serverSettings) {
        myServerSettings = serverSettings;
    }

    @NotNull
    @Override
    public String getId() {
        return ID;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Use custom resource template (VM / KCP)";
    }

    @Nullable
    @Override
    public String getDescription() {
        return null;
    }

    @NotNull
    @Override
    public Pod getPodTemplate(@NotNull String instanceName,
                              @NotNull CloudInstanceUserData cloudInstanceUserData,
                              @NotNull KubeCloudImage kubeCloudImage,
                              @NotNull KubeApiConnector apiConnector) {
        throw new KubeCloudException("The custom resource deploy mode does not produce pods");
    }

    @NotNull
    public GenericKubernetesResource getResourceTemplate(@NotNull String instanceName,
                                                         @NotNull CloudInstanceUserData cloudInstanceUserData,
                                                         @NotNull KubeCloudImage kubeCloudImage,
                                                         @NotNull KubeApiConnector apiConnector) {
        String spec = kubeCloudImage.getCustomResourceTemplate();
        if (StringUtil.isEmpty(spec)) {
            throw new KubeCloudException("Custom resource template is not specified for image " + kubeCloudImage.getId());
        }
        final CustomResourceContext resourceContext = kubeCloudImage.getCustomResourceContext();
        if (resourceContext == null) {
            throw new KubeCloudException("Cannot determine the custom resource type for image " + kubeCloudImage.getId());
        }

        spec = substitutePlaceholders(spec, instanceName, cloudInstanceUserData, kubeCloudImage);

        final GenericKubernetesResource resource = Serialization.unmarshal(
          new ByteArrayInputStream(spec.getBytes()),
          GenericKubernetesResource.class
        );

        if (resource.getMetadata() == null) {
            resource.setMetadata(new ObjectMeta());
        }
        final ObjectMeta metadata = resource.getMetadata();
        metadata.setName(instanceName);
        if (!resourceContext.isClusterScoped() && StringUtil.isEmpty(metadata.getNamespace())) {
            metadata.setNamespace(apiConnector.getNamespace());
        }

        final Map<String, String> labels = new HashMap<>();
        if (metadata.getLabels() != null) {
            labels.putAll(metadata.getLabels());
        }
        labels.put(KubeTeamCityLabels.TEAMCITY_AGENT_LABEL, "");
        labels.put(KubeTeamCityLabels.TEAMCITY_SERVER_UUID, StringUtil.emptyIfNull(myServerSettings.getServerUUID()));
        labels.put(KubeTeamCityLabels.TEAMCITY_CLOUD_PROFILE, cloudInstanceUserData.getProfileId());
        labels.put(KubeTeamCityLabels.TEAMCITY_CLOUD_IMAGE, kubeCloudImage.getId());
        metadata.setLabels(labels);

        return resource;
    }

    @NotNull
    private String substitutePlaceholders(@NotNull String spec,
                                          @NotNull String instanceName,
                                          @NotNull CloudInstanceUserData cloudInstanceUserData,
                                          @NotNull KubeCloudImage kubeCloudImage) {
        return spec
          .replaceAll("%instance\\.id%", instanceName)
          .replaceAll("%agent\\.name%", kubeCloudImage.getAgentName(instanceName))
          .replaceAll("%server\\.url%", cloudInstanceUserData.getServerAddress())
          .replaceAll("%server\\.uuid%", StringUtil.emptyIfNull(myServerSettings.getServerUUID()))
          .replaceAll("%profile\\.id%", cloudInstanceUserData.getProfileId())
          .replaceAll("%image\\.id%", kubeCloudImage.getId());
    }

    @Nullable
    @Override
    public PersistentVolumeClaim getPVC(@NotNull final String instanceName,
                                        @NotNull final KubeCloudImage kubeCloudImage) {
        return null;
    }
}
