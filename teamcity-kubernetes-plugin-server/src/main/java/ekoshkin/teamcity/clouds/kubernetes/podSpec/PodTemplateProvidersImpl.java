package ekoshkin.teamcity.clouds.kubernetes.podSpec;

import ekoshkin.teamcity.clouds.kubernetes.KubeCloudException;
import jetbrains.buildServer.serverSide.ServerSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 15.06.17.
 */
public class PodTemplateProvidersImpl implements PodTemplateProviders {
    private final Map<String, PodTemplateProvider> myIdToProviderMap = new HashMap<>();

    public PodTemplateProvidersImpl(@NotNull ServerSettings serverSettings) {
        registerProvider(new SimpleRunContainerPodTemplateProvider(serverSettings));
        registerProvider(new CustomTemplatePodTemplateProvider());
        registerProvider(new DeploymentPodTemplateProvider(kubeApiConnector));
    }

    @NotNull
    @Override
    public Collection<PodTemplateProvider> getAll() {
        return myIdToProviderMap.values();
    }

    @Nullable
    @Override
    public PodTemplateProvider find(@Nullable String id) {
        return myIdToProviderMap.get(id);
    }

    @NotNull
    @Override
    public PodTemplateProvider get(@Nullable String id) {
        PodTemplateProvider podTemplateProvider = find(id);
        if(podTemplateProvider == null) throw new KubeCloudException("Unknown pod specification provider " + id);
        return podTemplateProvider;
    }

    private void registerProvider(PodTemplateProvider podTemplateProvider) {
        myIdToProviderMap.put(podTemplateProvider.getId(), podTemplateProvider);
    }
}
