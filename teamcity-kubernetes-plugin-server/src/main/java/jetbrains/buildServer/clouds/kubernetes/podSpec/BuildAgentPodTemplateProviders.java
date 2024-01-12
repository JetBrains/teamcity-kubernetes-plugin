
package jetbrains.buildServer.clouds.kubernetes.podSpec;

import jetbrains.buildServer.clouds.kubernetes.KubeCloudException;
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
    default BuildAgentPodTemplateProvider get(@Nullable String id){
        BuildAgentPodTemplateProvider podTemplateProvider = find(id);
        if(podTemplateProvider == null) throw new KubeCloudException("Unknown pod specification provider " + id);
        return podTemplateProvider;
    }
}