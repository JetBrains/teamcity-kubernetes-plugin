package ekoshkin.teamcity.clouds.kubernetes.web;

import jetbrains.buildServer.controllers.BaseFormXmlController;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 28.05.17.
 */
public class KubeProfileEditController extends BaseFormXmlController {

    public static final String EDIT_KUBE_HTML = "editKube.html";

    private PluginDescriptor myPluginDescriptor;

    public KubeProfileEditController(@NotNull final SBuildServer server,
                                     @NotNull final WebControllerManager web,
                                     @NotNull final PluginDescriptor pluginDescriptor) {
        super(server);
        myPluginDescriptor = pluginDescriptor;
        web.registerController(pluginDescriptor.getPluginResourcesPath(EDIT_KUBE_HTML), this);
    }

    @Override
    protected ModelAndView doGet(@NotNull HttpServletRequest httpServletRequest, @NotNull HttpServletResponse httpServletResponse) {
        return new ModelAndView(myPluginDescriptor.getPluginResourcesPath("editProfile.jsp"));
    }

    @Override
    protected void doPost(@NotNull HttpServletRequest httpServletRequest, @NotNull HttpServletResponse httpServletResponse, @NotNull Element element) {

    }
}
