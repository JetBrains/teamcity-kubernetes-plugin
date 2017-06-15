package ekoshkin.teamcity.clouds.kubernetes.podSpec;

import io.fabric8.kubernetes.api.model.Pod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 15.06.17.
 */
public interface PodSpecProvider {
    @NotNull String getId();
    @NotNull String getDisplayName();
    @Nullable String getDescription();
    @NotNull Pod getPodSpec();
}
