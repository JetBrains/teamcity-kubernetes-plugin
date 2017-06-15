package ekoshkin.teamcity.clouds.kubernetes.podSpec;

import ekoshkin.teamcity.clouds.kubernetes.KubeCloudException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 15.06.17.
 */
public class PodSpecProvidersImpl implements PodSpecProviders {
    private final Map<String, PodSpecProvider> myIdToProviderMap = new HashMap<>();

    public PodSpecProvidersImpl() {
        registerProvider(new SimpleRunContainerPodSpecProvider());
        registerProvider(new CustomTemplatePodSpecProvider());
        registerProvider(new DeploymentPodSpecProvider());
    }

    @NotNull
    @Override
    public Collection<PodSpecProvider> getAll() {
        return myIdToProviderMap.values();
    }

    @Nullable
    @Override
    public PodSpecProvider find(@Nullable String id) {
        return myIdToProviderMap.get(id);
    }

    @NotNull
    @Override
    public PodSpecProvider get(@Nullable String id) {
        PodSpecProvider podSpecProvider = find(id);
        if(podSpecProvider == null) throw new KubeCloudException("Unknown pod specification provider " + id);
        return podSpecProvider;
    }

    private void registerProvider(PodSpecProvider podSpecProvider) {
        myIdToProviderMap.put(podSpecProvider.getId(), podSpecProvider);
    }
}
