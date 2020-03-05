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

package jetbrains.buildServer.clouds.kubernetes.connector;

import com.intellij.openapi.diagnostic.Logger;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.*;
import java.util.Date;
import java.util.function.Function;
import jetbrains.buildServer.clouds.kubernetes.auth.KubeAuthStrategy;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.StringUtil;
import org.apache.commons.codec.binary.Base64;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 28.05.17.
 */
public class KubeApiConnectorImpl implements KubeApiConnector {
    private static final Logger LOG = Logger.getInstance(KubeApiConnectorImpl.class.getName());

    private static final int DEFAULT_CONNECTION_TIMEOUT_MS = 5 * 1000;
    private static final int DEFAULT_REQUEST_TIMEOUT_MS = 15 * 1000;

    private String myProfileId;
    @NotNull
    private final KubeApiConnection myConnectionSettings;
    private volatile KubernetesClient myKubernetesClient;
    private final KubeAuthStrategy myAuthStrategy;

    public KubeApiConnectorImpl(@NotNull String profileId,  @NotNull KubeApiConnection connectionSettings, @NotNull KubeAuthStrategy authStrategy) {
        myProfileId = profileId;
        myConnectionSettings = connectionSettings;
        myAuthStrategy = authStrategy;
        myKubernetesClient = createClient(createConfig(myConnectionSettings, myAuthStrategy));
    }

    @NotNull
    protected KubernetesClient createClient(@NotNull final Config config){
        LOG.info("Creating new client with config" + getConfigDescription(config));
        return new DefaultKubernetesClient(config);
    }


    protected Config createConfig(@NotNull KubeApiConnection connectionSettings, @NotNull KubeAuthStrategy authStrategy){
        ConfigBuilder configBuilder = new ConfigBuilder()
          .withMasterUrl(connectionSettings.getApiServerUrl())
          .withNamespace(connectionSettings.getNamespace())
          .withRequestTimeout(DEFAULT_REQUEST_TIMEOUT_MS)
          .withConnectionTimeout(DEFAULT_CONNECTION_TIMEOUT_MS);
        final String caCertData = connectionSettings.getCACertData();
        if(StringUtil.isEmptyOrSpaces(caCertData)){
            configBuilder.withTrustCerts(true);
        } else {
            configBuilder.withCaCertData(Base64.encodeBase64String(caCertData.getBytes()));
        }
        configBuilder = authStrategy.apply(configBuilder, connectionSettings);
        return configBuilder.build();
    }

    @NotNull
    @Override
    public KubeApiConnectionCheckResult testConnection() {
        try {
            String currentNamespaceName = myKubernetesClient.getNamespace();
            Namespace currentNamespace = myKubernetesClient.namespaces().withName(currentNamespaceName).get();
            return currentNamespace != null
                    ? KubeApiConnectionCheckResult.ok("Connection successful")
                    : KubeApiConnectionCheckResult.error(
                      String.format("Error connecting to %s: invalid namespace %s", myConnectionSettings.getApiServerUrl(), StringUtil.isEmptyOrSpaces(currentNamespaceName) ? "Default" : currentNamespaceName),
                    false);
        } catch (KubernetesClientException e) {
            return KubeApiConnectionCheckResult.error(String.format("Error connecting to %s: %s", myConnectionSettings.getApiServerUrl(), e.getCause() == null ? e.getMessage() : e.getCause().getMessage()),
                                                      e.getStatus().getCode() == 401);
        } catch (Exception e) {
            return KubeApiConnectionCheckResult.error(
              String.format("Error connecting to %s: %s", myConnectionSettings.getApiServerUrl(), e.getMessage()),
              false
            );
        }
    }

    @NotNull
    @Override
    public Pod createPod(@NotNull Pod podTemplate) {
        return withKubernetesClient(kubernetesClient -> kubernetesClient.pods().create(podTemplate));
    }

    @Override
    public PersistentVolumeClaim createPVC(@NotNull final PersistentVolumeClaim pvc) {
        return withKubernetesClient(kubernetesClient -> kubernetesClient.persistentVolumeClaims().create(pvc));
    }

    @Override
    public boolean deletePod(@NotNull String podName, long gracePeriod) {
        return withKubernetesClient(kubernetesClient -> kubernetesClient.pods().withName(podName).delete());
    }

    @NotNull
    @Override
    public Collection<Pod> listPods(@NotNull Map<String, String> labels) {
        return withKubernetesClient(kubernetesClient -> kubernetesClient.pods().withLabels(labels).list().getItems());
    }

    @Nullable
    @Override
    public Deployment getDeployment(@NotNull String deploymentName) {
        return withKubernetesClient(kubernetesClient -> kubernetesClient.apps().deployments().withName(deploymentName).get());
    }

    @Nullable
    @Override
    public PodStatus getPodStatus(@NotNull String podName) {
        return withKubernetesClient(kubernetesClient -> {
            final Pod podNow = kubernetesClient.pods().withName(podName).get();
            return podNow == null ? null : podNow.getStatus();
        });
    }

    @NotNull
    @Override
    public Collection<String> listNamespaces() {
        return withKubernetesClient(kubernetesClient -> {
            return CollectionsUtil.convertCollection(kubernetesClient.namespaces().list().getItems(), namespace -> namespace.getMetadata().getName());
        });
    }

    @NotNull
    @Override
    public Collection<String> listDeployments() {
        return withKubernetesClient(kubernetesClient -> {
            return CollectionsUtil.convertCollection(kubernetesClient.apps().deployments().list().getItems(), namespace -> namespace.getMetadata().getName());
        });
    }

    public boolean deletePVC(@NotNull String name){
        return withKubernetesClient(kubernetesClient -> kubernetesClient.persistentVolumeClaims().withName(name).delete());
    }

    @Override
    public void invalidate() {
        myAuthStrategy.invalidate(myConnectionSettings);
    }

    private <T> T withKubernetesClient(Function<KubernetesClient, T> function){
        return withKubernetesClient(false, function);
    }

    private <T> T withKubernetesClient(boolean retrying, Function<KubernetesClient, T> function){
        try {
            return function.apply(myKubernetesClient);
        } catch (KubernetesClientException kce){
            final String operation = getOperation(kce);
            LOG.warnAndDebugDetails(String.format("An error occurred at %s, ProfileId: %s, Code: %d, Status: %s", operation, myProfileId, kce.getCode(), kce.getStatus()),
                                    KubernetesClientException.launderThrowable(kce));
            if (!retrying && kce.getCode()==401){
                final KubeApiConnectionCheckResult result = testConnection();
                LOG.info(String.format("Test connection for %s: %s", myProfileId, result));
                if (result.isNeedRefresh()){
                    LOG.info("Will now invalidate and recreate client for " + myProfileId);
                    invalidate();
                    myKubernetesClient = createClient(createConfig(myConnectionSettings, myAuthStrategy));
                }
                return withKubernetesClient(true, function);
            }
            throw kce;
        }
    }

    @Nullable
    private String getOperation(@NotNull KubernetesClientException kce){
        final StackTraceElement[] stackTrace = kce.getStackTrace();
        for (int i=0; i<stackTrace.length; i++){
            if (stackTrace[i].getClassName().contains("jetbrains.buildServer")){
                return stackTrace[i].toString();
            }
        }
        return null;
    }

    private static String getConfigDescription(Config config){
        if (config.getOauthToken() != null) {
            String token = config.getOauthToken();
            String tokenMask;
            if (token.length() <= 16) {
                tokenMask = "Updated on  " + new Date();
            } else {
                tokenMask = String.format("%s...%s", token.substring(0, 4), token.substring(token.length() - 4));
            }
            return " with OAuthToken: " + tokenMask;
        }
        else
            return "";
    }
}