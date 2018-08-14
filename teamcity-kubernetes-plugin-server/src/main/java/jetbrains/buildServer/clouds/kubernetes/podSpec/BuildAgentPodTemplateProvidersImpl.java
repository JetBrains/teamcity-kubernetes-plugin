package jetbrains.buildServer.clouds.kubernetes.podSpec;

import jetbrains.buildServer.clouds.kubernetes.KubeCloudException;
import jetbrains.buildServer.serverSide.ServerSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 15.06.17.
 */
public class BuildAgentPodTemplateProvidersImpl implements BuildAgentPodTemplateProviders {
    private final Map<String, BuildAgentPodTemplateProvider> myIdToProviderMap = new HashMap<>();

    public BuildAgentPodTemplateProvidersImpl(@NotNull ServerSettings serverSettings,
                                              @NotNull DeploymentContentProvider deploymentContentProvider) {
        registerProvider(new SimpleRunContainerBuildAgentPodTemplateProvider(serverSettings));
        registerProvider(new DeploymentBuildAgentPodTemplateProvider(serverSettings, deploymentContentProvider));
        registerProvider(new CustomTemplatePodTemplateProvider(serverSettings));
    }

    @NotNull
    @Override
    public Collection<BuildAgentPodTemplateProvider> getAll() {
        return myIdToProviderMap.values();
    }

    @Nullable
    @Override
    public BuildAgentPodTemplateProvider find(@Nullable String id) {
        return myIdToProviderMap.get(id);
    }

    @NotNull
    @Override
    public BuildAgentPodTemplateProvider get(@Nullable String id) {
        BuildAgentPodTemplateProvider podTemplateProvider = find(id);
        if(podTemplateProvider == null) throw new KubeCloudException("Unknown pod specification provider " + id);
        return podTemplateProvider;
    }

    private void registerProvider(BuildAgentPodTemplateProvider podTemplateProvider) {
        myIdToProviderMap.put(podTemplateProvider.getId(), podTemplateProvider);
    }
}
