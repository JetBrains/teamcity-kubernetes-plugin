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

package jetbrains.buildServer.clouds.kubernetes.web;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.BuildProject;
import jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants;
import jetbrains.buildServer.clouds.kubernetes.auth.KubeAuthStrategy;
import jetbrains.buildServer.clouds.kubernetes.auth.KubeAuthStrategyProvider;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnection;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnectionCheckResult;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnectorImpl;
import jetbrains.buildServer.clouds.kubernetes.podSpec.BuildAgentPodTemplateProviders;
import jetbrains.buildServer.controllers.ActionErrors;
import jetbrains.buildServer.controllers.BaseFormXmlController;
import jetbrains.buildServer.controllers.BasePropertiesBean;
import jetbrains.buildServer.internal.PluginPropertiesUtil;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.agentPools.AgentPool;
import jetbrains.buildServer.serverSide.agentPools.AgentPoolManager;
import jetbrains.buildServer.serverSide.agentPools.AgentPoolUtil;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static jetbrains.buildServer.agent.Constants.SECURE_PROPERTY_PREFIX;
import static jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants.CA_CERT_DATA;
import static jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants.DEFAULT_NAMESPACE;
import static jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants.KUBERNETES_NAMESPACE;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 28.05.17.
 */
public class KubeProfileEditController extends BaseFormXmlController {
    private final static Logger LOG = Logger.getInstance(KubeProfileEditController.class.getName());

    public static final String EDIT_KUBE_HTML = "editKube.html";
    private final String myPath;

    private final PluginDescriptor myPluginDescriptor;
    private final AgentPoolManager myAgentPoolManager;
    private final KubeAuthStrategyProvider myAuthStrategyProvider;
    private final BuildAgentPodTemplateProviders myPodTemplateProviders;
    private final ChooserController.Namespaces myNamespacesChooser;
    private final ChooserController.Deployments myDeploymentsChooser;
    private final KubeDeleteImageDialogController myKubeDeleteImageDialogController;

    public KubeProfileEditController(@NotNull final SBuildServer server,
                                     @NotNull final WebControllerManager web,
                                     @NotNull final PluginDescriptor pluginDescriptor,
                                     @NotNull final AgentPoolManager agentPoolManager,
                                     @NotNull final KubeAuthStrategyProvider authStrategyProvider,
                                     @NotNull final BuildAgentPodTemplateProviders podTemplateProviders,
                                     @NotNull final ChooserController.Namespaces namespacesChooser,
                                     @NotNull final ChooserController.Deployments deploymentsChooser,
                                     @NotNull final KubeDeleteImageDialogController kubeDeleteImageDialogController) {
        super(server);
        myPluginDescriptor = pluginDescriptor;
        myPath = pluginDescriptor.getPluginResourcesPath(EDIT_KUBE_HTML);
        myAgentPoolManager = agentPoolManager;
        myAuthStrategyProvider = authStrategyProvider;
        myPodTemplateProviders = podTemplateProviders;
        myNamespacesChooser = namespacesChooser;
        myDeploymentsChooser = deploymentsChooser;
        myKubeDeleteImageDialogController = kubeDeleteImageDialogController;
        web.registerController(myPath, this);
    }

    @Override
    protected ModelAndView doGet(@NotNull HttpServletRequest httpServletRequest, @NotNull HttpServletResponse httpServletResponse) {
        ModelAndView modelAndView = new ModelAndView(myPluginDescriptor.getPluginResourcesPath("editProfile.jsp"));
        Map<String, Object> model = modelAndView.getModel();
        model.put("testConnectionUrl", myPath + "?testConnection=true");
        model.put("namespaceChooserUrl", myNamespacesChooser.getUrl());
        model.put("deploymentChooserUrl", myDeploymentsChooser.getUrl());
        model.put("deleteImageUrl", myKubeDeleteImageDialogController.getUrl());
        final String projectId = httpServletRequest.getParameter("projectId");

        final List<AgentPool> pools = new ArrayList<>();
        if (!BuildProject.ROOT_PROJECT_ID.equals(projectId)){
            pools.add(AgentPoolUtil.DUMMY_PROJECT_POOL);
        }
        pools.addAll(myAgentPoolManager.getProjectOwnedAgentPools(projectId));

        model.put("agentPools", pools);
        model.put("authStrategies", myAuthStrategyProvider.getAll());
        model.put("podTemplateProviders", myPodTemplateProviders.getAll());
        return modelAndView;
    }

    @Override
    protected void doPost(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Element xmlResponse) {
        BasePropertiesBean propsBean =  new BasePropertiesBean(null);
        PluginPropertiesUtil.bindPropertiesFromRequest(request, propsBean, true);
        final Map<String, String> props = propsBean.getProperties();
        if(Boolean.parseBoolean(request.getParameter("testConnection"))){
            KubeApiConnection connectionSettings = new KubeApiConnection() {
                @NotNull
                @Override
                public String getApiServerUrl() {
                    return props.get(KubeParametersConstants.API_SERVER_URL);
                }

                @NotNull
                @Override
                public String getNamespace() {
                    String explicitNameSpace = props.get(KUBERNETES_NAMESPACE);
                    return StringUtil.isEmpty(explicitNameSpace) ? DEFAULT_NAMESPACE : explicitNameSpace;
                }

                @Nullable
                @Override
                public String getCustomParameter(@NotNull String parameterName) {
                    return props.containsKey(parameterName) ? props.get(parameterName) : props.get(SECURE_PROPERTY_PREFIX + parameterName);
                }

                @Nullable
                @Override
                public String getCACertData() {
                    return props.get(SECURE_PROPERTY_PREFIX + CA_CERT_DATA);
                }
            };
            final String authStrategyName = props.get(KubeParametersConstants.AUTH_STRATEGY);
            KubeApiConnectorImpl apiConnector = null;
            try {
                final KubeAuthStrategy strategy = myAuthStrategyProvider.get(authStrategyName);
                apiConnector = new KubeApiConnectorImpl("editProfile", connectionSettings, strategy);
                KubeApiConnectionCheckResult connectionCheckResult = apiConnector.testConnection();
                if(!connectionCheckResult.isSuccess()){
                    if (strategy.isRefreshable() && connectionCheckResult.isNeedRefresh()){
                        apiConnector.invalidate();
                        connectionCheckResult = apiConnector.testConnection();
                        if (connectionCheckResult.isSuccess()){
                            return;
                        }
                    }
                    final String checkResultMessage = connectionCheckResult.getMessage();
                    LOG.debug("Error while checking connection to k8s API. " + checkResultMessage);
                    final ActionErrors errors = new ActionErrors();
                    errors.addError("connection", checkResultMessage);
                    writeErrors(xmlResponse, errors);
                }
            } catch (Exception ex){
                LOG.debug(ex);
                final ActionErrors errors = new ActionErrors();
                final String errorMessage;
                if (ex.getCause() != null) {
                    errorMessage = ex.getMessage() + ": " + ex.getCause().getMessage();
                } else {
                    errorMessage = ex.getMessage();
                }
                errors.addError("connection", errorMessage);
                writeErrors(xmlResponse, errors);
            } finally {
                FileUtil.close(apiConnector);
            }
        }
    }
}
