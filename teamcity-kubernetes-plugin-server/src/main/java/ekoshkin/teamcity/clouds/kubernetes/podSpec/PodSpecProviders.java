package ekoshkin.teamcity.clouds.kubernetes.podSpec;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 15.06.17.
 */
public interface PodSpecProviders {
    @NotNull
    Collection<PodSpecProvider> getAll();

    @Nullable
    PodSpecProvider find(@Nullable String id);

    @NotNull
    PodSpecProvider get(@Nullable String id);
}
