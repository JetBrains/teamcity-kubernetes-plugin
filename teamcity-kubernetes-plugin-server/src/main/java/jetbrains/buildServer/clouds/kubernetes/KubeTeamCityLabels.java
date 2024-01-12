
package jetbrains.buildServer.clouds.kubernetes;

import io.fabric8.kubernetes.api.model.Pod;
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

    public static void addCustomLabel(@NotNull Pod pod,
                               @NotNull String label,
                               @NotNull String value){
        final Map<String, String> labels = new HashMap<>(pod.getMetadata().getLabels());
        labels.put(label, value);
        pod.getMetadata().setLabels(labels);
    }
}