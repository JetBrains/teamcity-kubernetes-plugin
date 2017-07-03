package jetbrains.buildServer.clouds.kubernetes.podSpec;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 15.06.17.
 */
public interface PodTemplateProviders {
    @NotNull
    Collection<PodTemplateProvider> getAll();

    @Nullable
    PodTemplateProvider find(@Nullable String id);

    @NotNull
    PodTemplateProvider get(@Nullable String id);
}
