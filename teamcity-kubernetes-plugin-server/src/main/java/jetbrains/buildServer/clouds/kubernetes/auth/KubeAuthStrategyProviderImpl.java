/*
 * Copyright 2000-2022 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.clouds.kubernetes.auth;

import com.google.common.annotations.VisibleForTesting;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import jetbrains.buildServer.clouds.kubernetes.KubeCloudException;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import jetbrains.buildServer.util.TimeService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 14.06.17.
 */
public class KubeAuthStrategyProviderImpl implements KubeAuthStrategyProvider {
    private static final Map<String, KubeAuthStrategy> myIdToStrategyMap = new HashMap<>();

    public KubeAuthStrategyProviderImpl(@NotNull TimeService timeService) {
        registerStrategy(new UserPasswdAuthStrategy());
        registerStrategy(new DefaultServiceAccountAuthStrategy());
        registerStrategy(new UnauthorizedAccessStrategy());
        registerStrategy(new ClientCertificateAuthStrategy());
        registerStrategy(new TokenAuthStrategy());
        if (TeamCityProperties.getBoolean("teamcity.kubernetes.localKubeconfig.enable")) {
            registerStrategy(new KubeconfigAuthStrategy());
        }
        registerStrategy(new OIDCAuthStrategy(timeService));
        registerStrategy(new EKSAuthStrategy(timeService));
    }

    @NotNull
    public static Collection<KubeAuthStrategy> getAll(@NotNull String projectId) {
        return myIdToStrategyMap.values()
                                .stream()
                                .filter(strategy -> strategy.isAvailable(projectId))
                                .collect(Collectors.toList());
    }

    public static Map<String, Object> getAdditionalSettings(@NotNull String projectId){
        final HashMap<String, Object> additionalSettings = new HashMap<>();
        getAll(projectId).forEach(auth -> {
            auth.fillAdditionalSettings(additionalSettings, auth.isAvailable(projectId));
        });
        return additionalSettings;
    }

    @Nullable
    @Override
    public KubeAuthStrategy find(@Nullable String strategyId) {
        return myIdToStrategyMap.get(strategyId);
    }

    @NotNull
    @Override
    public KubeAuthStrategy get(@Nullable String id) {
        KubeAuthStrategy authStrategy = find(id);
        if(authStrategy == null) throw new KubeCloudException("Unknown auth strategy " + id);
        return authStrategy;
    }

    @VisibleForTesting
    public void registerStrategy(KubeAuthStrategy authStrategy){
        myIdToStrategyMap.put(authStrategy.getId(), authStrategy);
    }
}
