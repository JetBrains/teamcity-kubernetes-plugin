/*
 * Copyright 2000-2020 JetBrains s.r.o.
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

import io.fabric8.kubernetes.client.ConfigBuilder;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import jetbrains.buildServer.clouds.kubernetes.KubeCloudException;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnection;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants.AUTH_TOKEN;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 26.06.17.
 */
public class TokenAuthStrategy implements KubeAuthStrategy {
    @NotNull
    @Override
    public String getId() {
        return "token";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Token";
    }

    @Nullable
    @Override
    public String getDescription() {
        return "Authenticate with a bearer token";
    }

    @NotNull
    @Override
    public ConfigBuilder apply(@NotNull ConfigBuilder clientConfig, @NotNull KubeApiConnection connection) {
        String token = connection.getCustomParameter(SECURE_PREFIX + AUTH_TOKEN);
        if(StringUtil.isEmpty(token)) {
            throw new KubeCloudException("Auth token is empty for connection to " + connection.getApiServerUrl());
        }
        return clientConfig.withOauthToken(token);
    }

    @Override
    public Collection<InvalidProperty> process(final Map<String, String> properties) {
        if (StringUtil.isEmpty(properties.get(SECURE_PREFIX+AUTH_TOKEN))){
            return Collections.singletonList(new InvalidProperty( AUTH_TOKEN, "Token is required"));
        }
        return Collections.emptyList();
    }
}