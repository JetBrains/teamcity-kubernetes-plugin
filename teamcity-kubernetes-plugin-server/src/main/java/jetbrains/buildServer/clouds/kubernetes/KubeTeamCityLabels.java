
package jetbrains.buildServer.clouds.kubernetes;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 07.06.17.
 */
public class KubeTeamCityLabels {
    public static final String TEAMCITY_AGENT_LABEL = "teamcity-agent";
    public static final String TEAMCITY_SERVER_UUID = "teamcity-server-uuid";
    public static final String TEAMCITY_CLOUD_PROFILE = "teamcity-cloud-profile";
    public static final String TEAMCITY_CLOUD_IMAGE = "teamcity-cloud-image";
    public static final String POD_PVC_NAME = "pod-pvc-name";

    public static void addCustomLabel(@NotNull HasMetadata resource, @NotNull String label, @NotNull String value) {
        ObjectMeta metadata = resource.getMetadata();
        if (metadata == null) {
            resource.setMetadata(metadata = new ObjectMeta());
        }
        Map<String, String> labels = metadata.getLabels();
        if (labels == null) {
            labels = new HashMap<>();
            metadata.setLabels(labels);
        } else if (!(labels instanceof HashMap)) {
            labels = new HashMap<>(labels);
            metadata.setLabels(labels);
        }
        labels.put(label, value);
    }
}