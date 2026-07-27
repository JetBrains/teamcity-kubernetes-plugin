
package jetbrains.buildServer.clouds.kubernetes.web;

import java.util.Collection;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jetbrains.buildServer.clouds.kubernetes.RequestKubeApiConnection;
import jetbrains.buildServer.clouds.kubernetes.auth.KubeAuthStrategyProvider;
import jetbrains.buildServer.clouds.kubernetes.connection.KubernetesCredentialsFactory;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnection;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnector;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnectorImpl;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.IOGuard;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.SecurityContextEx;
import jetbrains.buildServer.serverSide.auth.SecurityContext;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 05.10.17.
 */
public abstract class ChooserController extends BaseController {

    private final ProjectManager myProjectManager;
    private final SecurityContext mySecurityContext;
    private final PluginDescriptor myPluginDescriptor;
    private final KubeAuthStrategyProvider myAuthStrategyProvider;
    private final KubernetesCredentialsFactory myCredentialsFactory;

    public ChooserController(@NotNull WebControllerManager web,
                             @NotNull PluginDescriptor pluginDescriptor,
                             @NotNull KubeAuthStrategyProvider authStrategyProvider,
                             @NotNull KubernetesCredentialsFactory credentialsFactory,
                             @NotNull String pluginResourcesPath,
                             @NotNull SecurityContext securityContext,
                             @NotNull ProjectManager projectManager) {
      myProjectManager = projectManager;
      myPluginDescriptor = pluginDescriptor;
      myAuthStrategyProvider = authStrategyProvider;
      myCredentialsFactory = credentialsFactory;
      mySecurityContext = securityContext;
      web.registerController(pluginDescriptor.getPluginResourcesPath(pluginResourcesPath), this);
    }

    @Nullable
    @Override
    protected ModelAndView doHandle(@NotNull HttpServletRequest httpServletRequest, @NotNull HttpServletResponse httpServletResponse) {

        checkPermissions(httpServletRequest);

        KubeApiConnection apiConnection = new RequestKubeApiConnection(httpServletRequest);
        ModelAndView modelAndView = new ModelAndView(myPluginDescriptor.getPluginResourcesPath(getJspName()));
        try (KubeApiConnector apiConnector
               = new KubeApiConnectorImpl("editProfile", apiConnection, myAuthStrategyProvider.get(apiConnection.getAuthStrategy()), myCredentialsFactory)){
            Collection<String> items = IOGuard.allowNetworkCall(() -> getItems(apiConnector));
            modelAndView.getModelMap().put(getItemsName(), items);
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

  private void checkPermissions(HttpServletRequest request) {
    String projectId = request.getParameter("projectId");
    SProject project = myProjectManager.findProjectByExternalId(projectId);
    if (project == null) {
      throw new IllegalArgumentException("Project was not found by projectId: " + projectId);
    }
    ((SecurityContextEx)mySecurityContext).getAccessChecker().checkCanEditProject(project);
  }

    @NotNull
    protected abstract Collection<String> getItems(KubeApiConnector apiConnector);

    @NotNull
    protected abstract String getItemsName();

    protected abstract String getJspName();

    public static class Deployments extends ChooserController{

      public static final String KUBE_DEPLOYMENTS_HTML = "kubeDeployments.html";
      private static String controllerUrl;

      public Deployments(@NotNull final WebControllerManager web,
                         @NotNull final PluginDescriptor pluginDescriptor,
                         @NotNull final KubeAuthStrategyProvider authStrategyProvider,
                         @NotNull final KubernetesCredentialsFactory kubernetesCredentialsFactory,
                         @NotNull SecurityContext securityContext,
                         @NotNull ProjectManager projectManager) {
            super(web, pluginDescriptor, authStrategyProvider, kubernetesCredentialsFactory, KUBE_DEPLOYMENTS_HTML, securityContext, projectManager);
            controllerUrl = pluginDescriptor.getPluginResourcesPath(KUBE_DEPLOYMENTS_HTML);
        }

        @NotNull
        @Override
        protected Collection<String> getItems(final KubeApiConnector apiConnector) {
            return apiConnector.listDeployments();
        }

        @NotNull
        @Override
        protected String getItemsName() {
            return "deployments";
        }

        protected String getJspName() {
            return "kubeDeployments.jsp";
        }

        public static String getControllerUrl() {
          return controllerUrl;
        }
    }

    public static class Namespaces extends ChooserController{

      public static final String KUBE_NAMESPACES_HTML = "kubeNamespaces.html";
      private static String controllerUrl;

      public Namespaces(@NotNull final WebControllerManager web,
                        @NotNull final PluginDescriptor pluginDescriptor,
                        @NotNull final KubeAuthStrategyProvider authStrategyProvider,
                        @NotNull final KubernetesCredentialsFactory kubernetesCredentialsFactory,
                        @NotNull SecurityContext securityContext,
                        @NotNull ProjectManager projectManager) {
          super(web, pluginDescriptor, authStrategyProvider, kubernetesCredentialsFactory, KUBE_NAMESPACES_HTML, securityContext, projectManager);
          controllerUrl = pluginDescriptor.getPluginResourcesPath(KUBE_NAMESPACES_HTML);
        }

        @NotNull
        @Override
        protected Collection<String> getItems(final KubeApiConnector apiConnector) {
            return apiConnector.listNamespaces();
        }

        @NotNull
        @Override
        protected String getItemsName() {
            return "namespaces";
        }

        @Override
        protected String getJspName() {
            return "kubeNamespaces.jsp";
        }

        public static String getControllerUrl() {
          return controllerUrl;
        }
    }
}