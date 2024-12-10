
package jetbrains.buildServer.clouds.kubernetes.web;

import com.intellij.openapi.diagnostic.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jetbrains.buildServer.BuildProject;
import jetbrains.buildServer.clouds.kubernetes.RequestKubeApiConnection;
import jetbrains.buildServer.clouds.kubernetes.auth.KubeAuthStrategy;
import jetbrains.buildServer.clouds.kubernetes.auth.KubeAuthStrategyProvider;
import jetbrains.buildServer.clouds.kubernetes.connection.KubernetesCredentialsFactory;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnection;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnectionCheckResult;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnectorImpl;
import jetbrains.buildServer.clouds.kubernetes.podSpec.BuildAgentPodTemplateProviders;
import jetbrains.buildServer.controllers.ActionErrors;
import jetbrains.buildServer.controllers.AuthorizationInterceptor;
import jetbrains.buildServer.controllers.BaseFormXmlController;
import jetbrains.buildServer.controllers.RequestPermissionsCheckerEx;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.agentPools.AgentPool;
import jetbrains.buildServer.serverSide.agentPools.AgentPoolManager;
import jetbrains.buildServer.serverSide.agentPools.AgentPoolUtil;
import jetbrains.buildServer.serverSide.auth.AccessDeniedException;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 28.05.17.
 */
public class KubeProfileEditController extends BaseFormXmlController {
    private final static Logger LOG = Logger.getInstance(KubeProfileEditController.class.getName());

    public static final String EDIT_KUBE_HTML = "editKube.html";

    private final PluginDescriptor myPluginDescriptor;
    @NotNull private final KubernetesCredentialsFactory myCredentialsFactory;
    private final AgentPoolManager myAgentPoolManager;
    private final KubeAuthStrategyProvider myAuthStrategyProvider;
    private final BuildAgentPodTemplateProviders myPodTemplateProviders;
    private final ChooserController.Namespaces myNamespacesChooser;
    private final ChooserController.Deployments myDeploymentsChooser;

  static {
        try {
            Class.forName("io.fabric8.kubernetes.client.Config");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public KubeProfileEditController(@NotNull final SBuildServer server,
                                     @NotNull final WebControllerManager web,
                                     @NotNull final PluginDescriptor pluginDescriptor,
                                     @NotNull final AgentPoolManager agentPoolManager,
                                     @NotNull final KubeAuthStrategyProvider authStrategyProvider,
                                     @NotNull final BuildAgentPodTemplateProviders podTemplateProviders,
                                     @NotNull final ChooserController.Namespaces namespacesChooser,
                                     @NotNull final ChooserController.Deployments deploymentsChooser,
                                     @NotNull final KubernetesCredentialsFactory credentialsFactory,
                                     @NotNull final AuthorizationInterceptor authInterceptor,
                                     @NotNull final ProjectManager projectManager){
        super(server);
        myPluginDescriptor = pluginDescriptor;
        myCredentialsFactory = credentialsFactory;
        String path = pluginDescriptor.getPluginResourcesPath(EDIT_KUBE_HTML);
        myAgentPoolManager = agentPoolManager;
        myAuthStrategyProvider = authStrategyProvider;
        myPodTemplateProviders = podTemplateProviders;
        myNamespacesChooser = namespacesChooser;
        myDeploymentsChooser = deploymentsChooser;
        web.registerController(path, this);
        authInterceptor.addPathBasedPermissionsChecker(path, new RequestPermissionsCheckerEx() {
            @Override
            public void checkPermissions(@NotNull SecurityContextEx securityContext, @NotNull HttpServletRequest request) {
                if (!isTestConnection(request)) {
                    return;
                }

                final String projectId = request.getParameter("projectId");
                final SProject project = projectManager.findProjectByExternalId(projectId);
                if (project == null) {
                    throw new AccessDeniedException(securityContext.getAuthorityHolder(), String.format("No project with id '%s' found", projectId));
                } else {
                    securityContext.getAccessChecker().checkCanEditProject(project);
                }
            }
        });
    }

    @Override
    protected ModelAndView doGet(@NotNull HttpServletRequest httpServletRequest, @NotNull HttpServletResponse httpServletResponse) {
        ModelAndView modelAndView = new ModelAndView(myPluginDescriptor.getPluginResourcesPath("editProfile.jsp"));
        Map<String, Object> model = modelAndView.getModel();
        model.put("namespaceChooserUrl", myNamespacesChooser.getUrl());
        model.put("deploymentChooserUrl", myDeploymentsChooser.getUrl());
        final String projectId = httpServletRequest.getParameter("projectId");

        final List<AgentPool> pools = new ArrayList<>();
        if (!BuildProject.ROOT_PROJECT_ID.equals(projectId)){
            pools.add(AgentPoolUtil.DUMMY_PROJECT_POOL);
        }
        pools.addAll(myAgentPoolManager.getProjectOwnedAgentPools(projectId));

        model.put("agentPools", pools);

        model.put("podTemplateProviders", myPodTemplateProviders.getAll());
        return modelAndView;
    }

    @Override
    protected void doPost(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Element xmlResponse) {

        if(isTestConnection(request)){
            final KubeApiConnection connectionSettings = new RequestKubeApiConnection(request);

            final KubeAuthStrategy strategy = myAuthStrategyProvider.get(connectionSettings.getAuthStrategy());
            try (final KubeApiConnectorImpl apiConnector = new KubeApiConnectorImpl("editProfile", connectionSettings, strategy, myCredentialsFactory)) {
                KubeApiConnectionCheckResult connectionCheckResult = IOGuard.allowNetworkCall(()->apiConnector.testConnection());
                if(!connectionCheckResult.isSuccess()){
                    if (strategy.isRefreshable() && connectionCheckResult.isNeedRefresh()){
                        final KubeApiConnectionCheckResult retryResult = IOGuard.allowNetworkCall(()->{
                            apiConnector.invalidate();
                            return apiConnector.testConnection();
                        });
                        if (retryResult.isSuccess()){
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
            }
        }
    }

    private static boolean isTestConnection(@NotNull HttpServletRequest request) {
        return Boolean.parseBoolean(request.getParameter("testConnection"));
    }
}