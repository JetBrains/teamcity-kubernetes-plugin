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

package jetbrains.buildServer.clouds.kubernetes;

import jetbrains.buildServer.clouds.CloudClientParameters;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import static jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants.*;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 29.05.17.
 */
public class KubeCloudClientParametersImpl implements KubeCloudClientParameters {
    private final CloudClientParameters myParameters;

    public KubeCloudClientParametersImpl(CloudClientParameters parameters) {
        myParameters = parameters;
    }

    @NotNull
    public static KubeCloudClientParametersImpl create(@NotNull CloudClientParameters genericCloudClientParameters) {
        return new KubeCloudClientParametersImpl(genericCloudClientParameters);
    }

    @NotNull
    @Override
    public String getApiServerUrl() {
        return myParameters.getParameter(API_SERVER_URL);
    }

    @NotNull
    @Override
    public String getNamespace(){
        String explicitNameSpace = myParameters.getParameter(KUBERNETES_NAMESPACE);
        return StringUtil.isEmpty(explicitNameSpace) ? DEFAULT_NAMESPACE : explicitNameSpace;
    }

    @Nullable
    @Override
    public String getCustomParameter(@NotNull String parameterName) {
        return myParameters.getParameter(parameterName);
    }

    @Nullable
    @Override
    public String getCACertData() {
        return myParameters.getParameter(CA_CERT_DATA);
    }

    @NotNull
    @Override
    public Collection<KubeCloudImageData> getImages(){
        return CollectionsUtil.convertCollection(myParameters.getCloudImages(), KubeCloudImageData::new);
    }

    @Override
    @NotNull
    public String getAuthStrategy() {
        return myParameters.getParameter(AUTH_STRATEGY);
    }

    public int getInstanceLimit() {
        String parameter = myParameters.getParameter(PROFILE_INSTANCE_LIMIT);
        return StringUtil.isEmpty(parameter) ? -1 : Integer.valueOf(parameter);
    }
}
