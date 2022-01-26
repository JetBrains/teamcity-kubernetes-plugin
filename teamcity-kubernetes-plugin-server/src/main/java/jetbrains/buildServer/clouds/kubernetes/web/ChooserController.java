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

package jetbrains.buildServer.clouds.kubernetes.web;

import java.util.Collection;
import jetbrains.buildServer.clouds.kubernetes.auth.KubeAuthStrategyProvider;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnection;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnector;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnectorImpl;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.controllers.BasePropertiesBean;
import jetbrains.buildServer.internal.PluginPropertiesUtil;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Map;

import static jetbrains.buildServer.agent.Constants.SECURE_PROPERTY_PREFIX;
import static jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants.*;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 05.10.17.
 */
public abstract class ChooserController extends BaseController {

    private final PluginDescriptor myPluginDescriptor;
    private final KubeAuthStrategyProvider myAuthStrategyProvider;

    public ChooserController(WebControllerManager web,
                             PluginDescriptor pluginDescriptor,
                             KubeAuthStrategyProvider authStrategyProvider) {
        myPluginDescriptor = pluginDescriptor;
        myAuthStrategyProvider = authStrategyProvider;
        web.registerController(getUrl(), this);
    }

    @NotNull
    public String getUrl() {
        return myPluginDescriptor.getPluginResourcesPath(getHtmlName());
    }

    @Nullable
    @Override
    protected ModelAndView doHandle(@NotNull HttpServletRequest httpServletRequest, @NotNull HttpServletResponse httpServletResponse) throws Exception {
        BasePropertiesBean propsBean = new BasePropertiesBean(null);
        PluginPropertiesUtil.bindPropertiesFromRequest(httpServletRequest, propsBean, true);
        Map<String, String> props = propsBean.getProperties();

        KubeApiConnection apiConnection = new KubeApiConnection() {
            @NotNull
            @Override
            public String getApiServerUrl() {
                return props.get(API_SERVER_URL);
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
        String authStrategy = props.get(AUTH_STRATEGY);

        ModelAndView modelAndView = new ModelAndView(myPluginDescriptor.getPluginResourcesPath(getJspName()));
        try {
            KubeApiConnector apiConnector = new KubeApiConnectorImpl("editProfile", apiConnection, myAuthStrategyProvider.get(authStrategy));
            modelAndView.getModelMap().put(getItemsName(), getItems(apiConnector));
            modelAndView.getModelMap().put("error","");
        } catch (Exception ex){
            modelAndView.getModelMap().put(getItemsName(), Collections.emptyList());
            if (ex.getCause() != null) {
                modelAndView.getModelMap().put("error", ex.getCause().getLocalizedMessage());
            } else {
                modelAndView.getModelMap().put("error", ex.getLocalizedMessage());
            }
        }
        return modelAndView;
    }

    @NotNull
    protected abstract Collection<String> getItems(KubeApiConnector apiConnector);

    @NotNull
    protected abstract String getItemsName();

    @NotNull
    protected abstract String getHtmlName();
    protected abstract String getJspName();

    public static class Deployments extends ChooserController{

        public Deployments(final WebControllerManager web,
                           final PluginDescriptor pluginDescriptor,
                           final KubeAuthStrategyProvider authStrategyProvider) {
            super(web, pluginDescriptor, authStrategyProvider);
        }

        @Override
        protected Collection<String> getItems(final KubeApiConnector apiConnector) {
            return apiConnector.listDeployments();
        }

        @Override
        protected String getItemsName() {
            return "deployments";
        }

        @Override
        protected String getHtmlName() {
            return "kubeDeployments.html";
        }

        protected String getJspName() {
            return "kubeDeployments.jsp";
        }
    }

    public static class Namespaces extends ChooserController{

        public Namespaces(final WebControllerManager web,
                          final PluginDescriptor pluginDescriptor,
                          final KubeAuthStrategyProvider authStrategyProvider) {
            super(web, pluginDescriptor, authStrategyProvider);
        }

        @Override
        protected Collection<String> getItems(final KubeApiConnector apiConnector) {
            return apiConnector.listNamespaces();
        }

        @Override
        protected String getItemsName() {
            return "namespaces";
        }

        @Override
        protected String getHtmlName() {
            return "kubeNamespaces.html";
        }

        @Override
        protected String getJspName() {
            return "kubeNamespaces.jsp";
        }
    }
}
