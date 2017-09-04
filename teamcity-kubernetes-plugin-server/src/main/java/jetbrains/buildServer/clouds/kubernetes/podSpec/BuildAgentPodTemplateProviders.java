package jetbrains.buildServer.clouds.kubernetes.podSpec;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 15.06.17.
 */
public interface BuildAgentPodTemplateProviders {
    @NotNull
    Collection<BuildAgentPodTemplateProvider> getAll();

    @Nullable
    BuildAgentPodTemplateProvider find(@Nullable String id);

    @NotNull
    BuildAgentPodTemplateProvider get(@Nullable String id);
}
