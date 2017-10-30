package jetbrains.buildServer.clouds.kubernetes.web;

import jetbrains.buildServer.clouds.kubernetes.auth.KubeAuthStrategyProvider;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnection;
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

import static jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants.*;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 05.10.17.
 */
public class KubeDeploymentChooserController extends BaseController {
    private static final String URL = "kubeDeployments.html";

    private final PluginDescriptor myPluginDescriptor;
    private KubeAuthStrategyProvider myAuthStrategyProvider;

    public KubeDeploymentChooserController(WebControllerManager web,
                                           PluginDescriptor pluginDescriptor,
                                           KubeAuthStrategyProvider authStrategyProvider) {
        myPluginDescriptor = pluginDescriptor;
        myAuthStrategyProvider = authStrategyProvider;
        web.registerController(getUrl(), this);
    }

    @NotNull
    public String getUrl() {
        return myPluginDescriptor.getPluginResourcesPath(URL);
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
                return props.get(parameterName);
            }
        };
        String authStrategy = props.get(AUTH_STRATEGY);

        ModelAndView modelAndView = new ModelAndView(myPluginDescriptor.getPluginResourcesPath("kubeDeployments.jsp"));
        try {
            KubeApiConnectorImpl apiConnector = KubeApiConnectorImpl.create(apiConnection, myAuthStrategyProvider.get(authStrategy));
            modelAndView.getModelMap().put("deployments", apiConnector.listDeployments());
            modelAndView.getModelMap().put("error","");
        } catch (Exception ex){
            modelAndView.getModelMap().put("deployments", Collections.emptyList());
            modelAndView.getModelMap().put("error", ex.getLocalizedMessage());
        }
        return modelAndView;
    }
}
