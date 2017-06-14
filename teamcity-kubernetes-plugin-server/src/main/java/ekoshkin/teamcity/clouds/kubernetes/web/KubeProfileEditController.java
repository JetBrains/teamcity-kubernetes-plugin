package ekoshkin.teamcity.clouds.kubernetes.web;

import ekoshkin.teamcity.clouds.internal.PluginPropertiesUtil;
import ekoshkin.teamcity.clouds.kubernetes.KubeParametersConstants;
import ekoshkin.teamcity.clouds.kubernetes.auth.KubeAuthStrategyProvider;
import ekoshkin.teamcity.clouds.kubernetes.connector.KubeApiConnection;
import ekoshkin.teamcity.clouds.kubernetes.connector.KubeApiConnectionCheckResult;
import ekoshkin.teamcity.clouds.kubernetes.connector.KubeApiConnectorImpl;
import jetbrains.buildServer.controllers.ActionErrors;
import jetbrains.buildServer.controllers.BaseFormXmlController;
import jetbrains.buildServer.controllers.BasePropertiesBean;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.agentPools.AgentPoolManager;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 28.05.17.
 */
public class KubeProfileEditController extends BaseFormXmlController {

    public static final String EDIT_KUBE_HTML = "editKube.html";
    private final String myPath;

    private final PluginDescriptor myPluginDescriptor;
    private final AgentPoolManager myAgentPoolManager;
    private final KubeAuthStrategyProvider myAuthStrategyProvider;

    public KubeProfileEditController(@NotNull final SBuildServer server,
                                     @NotNull final WebControllerManager web,
                                     @NotNull final PluginDescriptor pluginDescriptor,
                                     @NotNull final AgentPoolManager agentPoolManager, KubeAuthStrategyProvider authStrategyProvider) {
        super(server);
        myPluginDescriptor = pluginDescriptor;
        myPath = pluginDescriptor.getPluginResourcesPath(EDIT_KUBE_HTML);
        myAgentPoolManager = agentPoolManager;
        myAuthStrategyProvider = authStrategyProvider;
        web.registerController(myPath, this);
    }

    @Override
    protected ModelAndView doGet(@NotNull HttpServletRequest httpServletRequest, @NotNull HttpServletResponse httpServletResponse) {
        ModelAndView modelAndView = new ModelAndView(myPluginDescriptor.getPluginResourcesPath("editProfile.jsp"));
        Map<String, Object> model = modelAndView.getModel();
        model.put("testConnectionUrl", myPath + "?testConnection=true");
        final String projectId = httpServletRequest.getParameter("projectId");
        model.put("agentPools", myAgentPoolManager.getProjectOwnedAgentPools(projectId));
        return modelAndView;
    }

    @Override
    protected void doPost(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Element xmlResponse) {
        BasePropertiesBean propsBean =  new BasePropertiesBean(null);
        PluginPropertiesUtil.bindPropertiesFromRequest(request, propsBean, true);
        final Map<String, String> props = propsBean.getProperties();
        final String apiServerUrl = props.get(KubeParametersConstants.API_SERVER_URL);
        final String authStrategy = props.get(KubeParametersConstants.AUTH_STRATEGY);

        if(Boolean.parseBoolean(request.getParameter("testConnection"))){
            KubeApiConnection connectionSettings = new KubeApiConnection() {
                @NotNull
                @Override
                public String getApiServerUrl() {
                    return apiServerUrl;
                }

                @Nullable
                @Override
                public String getNamespace() {
                    return null;
                }
            };
            KubeApiConnectionCheckResult connectionCheckResult = new KubeApiConnectorImpl(connectionSettings, myAuthStrategyProvider.get(authStrategy)).testConnection();
            if(!connectionCheckResult.isSuccess()){
                final ActionErrors errors = new ActionErrors();
                errors.addError("connection", connectionCheckResult.getMessage());
                writeErrors(xmlResponse, errors);
            }
        }
    }
}
