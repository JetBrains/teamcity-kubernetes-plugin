
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
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 05.10.17.
 */
public abstract class ChooserController extends BaseController {

    private final PluginDescriptor myPluginDescriptor;
    private final KubeAuthStrategyProvider myAuthStrategyProvider;
    private final KubernetesCredentialsFactory myCredentialsFactory;

    public ChooserController(WebControllerManager web,
                             PluginDescriptor pluginDescriptor,
                             KubeAuthStrategyProvider authStrategyProvider,
                             KubernetesCredentialsFactory credentialsFactory) {
        myPluginDescriptor = pluginDescriptor;
        myAuthStrategyProvider = authStrategyProvider;
        myCredentialsFactory = credentialsFactory;
        web.registerController(getUrl(), this);
    }

    @NotNull
    public String getUrl() {
        return myPluginDescriptor.getPluginResourcesPath(getHtmlName());
    }

    @Nullable
    @Override
    protected ModelAndView doHandle(@NotNull HttpServletRequest httpServletRequest, @NotNull HttpServletResponse httpServletResponse) throws Exception {
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
                           final KubeAuthStrategyProvider authStrategyProvider,
                           final KubernetesCredentialsFactory kubernetesCredentialsFactory) {
            super(web, pluginDescriptor, authStrategyProvider, kubernetesCredentialsFactory);
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
                          final KubeAuthStrategyProvider authStrategyProvider,
                          final KubernetesCredentialsFactory kubernetesCredentialsFactory)  {
            super(web, pluginDescriptor, authStrategyProvider, kubernetesCredentialsFactory);
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