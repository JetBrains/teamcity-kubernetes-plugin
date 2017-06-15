package ekoshkin.teamcity.clouds.kubernetes.podSpec;

import io.fabric8.kubernetes.api.model.Pod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 15.06.17.
 */
public class CustomTemplatePodSpecProvider implements PodSpecProvider {
    @NotNull
    @Override
    public String getId() {
        return "custom-pod-template";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Use custom pod template";
    }

    @Nullable
    @Override
    public String getDescription() {
        return null;
    }

    @NotNull
    @Override
    public Pod getPodSpec() {
        return null;
    }
}
