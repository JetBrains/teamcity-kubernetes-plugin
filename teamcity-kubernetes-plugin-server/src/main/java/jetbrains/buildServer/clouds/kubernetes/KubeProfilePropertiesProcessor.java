
package jetbrains.buildServer.clouds.kubernetes;

import jetbrains.buildServer.clouds.kubernetes.auth.KubeAuthStrategy;
import jetbrains.buildServer.clouds.kubernetes.auth.KubeAuthStrategyProvider;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.util.StringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

import static jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants.API_SERVER_URL;
import static jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants.AUTH_STRATEGY;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 27.05.17.
 */
public class KubeProfilePropertiesProcessor implements PropertiesProcessor {

    @NotNull private final KubeAuthStrategyProvider myKubeAuthStrategyProvider;

    public KubeProfilePropertiesProcessor(@NotNull final KubeAuthStrategyProvider kubeAuthStrategyProvider) {
        myKubeAuthStrategyProvider = kubeAuthStrategyProvider;
    }

    @Override
    public Collection<InvalidProperty> process(Map<String, String> map) {
        Collection<InvalidProperty> invalids = new ArrayList<>();
        final String authStrategy = map.get(AUTH_STRATEGY);
        if (StringUtil.isEmptyOrSpaces(authStrategy)) {
            invalids.add(new InvalidProperty(AUTH_STRATEGY, "Authentication strategy must be selected"));
            return invalids;
        }
        KubeAuthStrategy strategy = myKubeAuthStrategyProvider.find(authStrategy);
        if (strategy != null) {
            if(strategy.requiresServerUrl() && StringUtil.isEmptyOrSpaces(map.get(API_SERVER_URL))) {
                invalids.add(new InvalidProperty(API_SERVER_URL, "Kubernetes API server URL must not be empty"));
            }
            Collection<InvalidProperty> strategyCollection = strategy.process(map);
            if (strategyCollection != null && !strategyCollection.isEmpty()){
                invalids.addAll(strategyCollection);
            }
        }

        return invalids;
    }
}