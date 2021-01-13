/*
 * Copyright 2000-2021 JetBrains s.r.o.
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

import jetbrains.buildServer.clouds.kubernetes.KubeCloudException;
import jetbrains.buildServer.util.TimeService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 14.06.17.
 */
public class KubeAuthStrategyProviderImpl implements KubeAuthStrategyProvider {
    private final Map<String, KubeAuthStrategy> myIdToStrategyMap = new HashMap<>();

    public KubeAuthStrategyProviderImpl(@NotNull TimeService timeService) {
        registerStrategy(new UserPasswdAuthStrategy());
        registerStrategy(new DefaultServiceAccountAuthStrategy());
        registerStrategy(new UnauthorizedAccessStrategy());
        registerStrategy(new ClientCertificateAuthStrategy());
        registerStrategy(new TokenAuthStrategy());
        registerStrategy(new OIDCAuthStrategy(timeService));
        registerStrategy(new EKSAuthStrategy(timeService));
    }

    @NotNull
    @Override
    public Collection<KubeAuthStrategy> getAll() {
        return myIdToStrategyMap.values();
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

    private void registerStrategy(KubeAuthStrategy authStrategy){
        myIdToStrategyMap.put(authStrategy.getId(), authStrategy);
    }
}
