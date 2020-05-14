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
import jetbrains.buildServer.clouds.kubernetes.KubeUtils;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnection;
import jetbrains.buildServer.util.StringUtil;
import org.apache.commons.codec.binary.Base64;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants.CLIENT_CERTIFICATE_DATA;
import static jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants.CLIENT_KEY_DATA;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 26.06.17.
 */
public class ClientCertificateAuthStrategy implements KubeAuthStrategy {
    @NotNull
    @Override
    public String getId() {
        return "client-cert";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Client certificate & key";
    }

    @Nullable
    @Override
    public String getDescription() {
        return "Authenticate with the client certificate & the key";
    }

    @NotNull
    @Override
    public ConfigBuilder apply(@NotNull ConfigBuilder clientConfig, @NotNull KubeApiConnection connection) {
        String clientCertData = connection.getCustomParameter(CLIENT_CERTIFICATE_DATA);
        String clientKeyData = connection.getCustomParameter(CLIENT_KEY_DATA);
        if(StringUtil.isEmpty(clientCertData)) {
            throw new KubeCloudException("Client certificate data is empty");
        }
        if(StringUtil.isEmpty(clientKeyData)) {
            throw new KubeCloudException("Client key data is empty");
        }
        return clientConfig.withClientCertData(KubeUtils.encodeBase64IfNecessary(clientCertData))
                           .withClientKeyData(KubeUtils.encodeBase64IfNecessary(clientKeyData));
    }
}
