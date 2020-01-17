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
import jetbrains.buildServer.clouds.kubernetes.KubeCloudException;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnection;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 14.06.17.
 */
public class DefaultServiceAccountAuthStrategy implements KubeAuthStrategy {
    private static final String DEFAULT_SERVICE_ACCOUNT_TOKEN_FILE = "/var/run/secrets/kubernetes.io/serviceaccount/token";

    @NotNull
    @Override
    public String getId() {
        return "service-account";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Default Service Account";
    }

    @Nullable
    @Override
    public String getDescription() {
        return null;
    }

    @NotNull
    @Override
    public ConfigBuilder apply(@NotNull ConfigBuilder clientConfig, @NotNull KubeApiConnection connection) {
        String defaultServiceAccountAuthToken = getDefaultServiceAccountAuthToken();
        if(StringUtil.isEmpty(defaultServiceAccountAuthToken)) throw new KubeCloudException("Can't locate default Kubernetes service account token.");
        return clientConfig.withOauthToken(defaultServiceAccountAuthToken);
    }

    @Nullable
    private String getDefaultServiceAccountAuthToken() {
        try {
            return FileUtil.readText(new File(DEFAULT_SERVICE_ACCOUNT_TOKEN_FILE));
        } catch (IOException e) {
            return null;
        }
    }
}
