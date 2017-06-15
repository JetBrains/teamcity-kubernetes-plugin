package ekoshkin.teamcity.clouds.kubernetes.podSpec;

import io.fabric8.kubernetes.api.model.Pod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 15.06.17.
 */
public class DeploymentPodSpecProvider implements PodSpecProvider {
    @NotNull
    @Override
    public String getId() {
        return "deployment-base";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Use pod template from deployment";
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
