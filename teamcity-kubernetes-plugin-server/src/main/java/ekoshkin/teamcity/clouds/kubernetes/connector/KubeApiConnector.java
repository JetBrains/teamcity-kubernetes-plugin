package ekoshkin.teamcity.clouds.kubernetes.connector;

import io.fabric8.kubernetes.api.model.Pod;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 28.05.17.
 */
public interface KubeApiConnector {
    String NEVER_RESTART_POLICY = "Never";

    @NotNull
    KubeApiConnectionCheckResult testConnection();

    @NotNull
    Pod createPod(@NotNull Pod podTemplate);

    boolean deletePod(@NotNull Pod pod);

    @NotNull
    Collection<Pod> listPods(String... labels);

    @NotNull
    PodPhase getPodPhase(@NotNull Pod pod);
}
