package jetbrains.buildServer.helm;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 29.11.17.
 */
public class HelmCommandArgumentsProvidersImpl implements HelmCommandArgumentsProviders {
    private final Map<String, HelmCommandArgumentsProvider> myCommandIdToProviderMap = new HashMap<String, HelmCommandArgumentsProvider>();

    public HelmCommandArgumentsProvidersImpl() {
        registerProvider(new DeleteArgumentsProvider());
        registerProvider(new InstallArgumentsProvider());
        registerProvider(new RollbackArgumentsProvider());
        registerProvider(new TestArgumentsProvider());
        registerProvider(new UpgradeArgumentsProvider());
    }

    @Nullable
    public HelmCommandArgumentsProvider find(@NotNull String commandId) {
        return myCommandIdToProviderMap.get(commandId);
    }

    private void registerProvider(HelmCommandArgumentsProvider provider) {
        myCommandIdToProviderMap.put(provider.getCommandId(), provider);
    }
}
