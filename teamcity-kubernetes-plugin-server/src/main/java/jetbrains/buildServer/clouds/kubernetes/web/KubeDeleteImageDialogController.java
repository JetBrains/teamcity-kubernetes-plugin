package jetbrains.buildServer.clouds.kubernetes.web;

import jetbrains.buildServer.clouds.CloudClientEx;
import jetbrains.buildServer.clouds.CloudImage;
import jetbrains.buildServer.clouds.server.CloudManagerBase;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 05.10.17.
 */
public class KubeDeleteImageDialogController extends BaseController {
    public static final String URL = "deleteKubeImage.html";

    private final PluginDescriptor myPluginDescriptor;
    private final CloudManagerBase myCloudManager;

    public KubeDeleteImageDialogController(WebControllerManager web,
                                           PluginDescriptor pluginDescriptor,
                                           CloudManagerBase cloudManager) {
        myPluginDescriptor = pluginDescriptor;
        myCloudManager = cloudManager;
        web.registerController(getUrl(), this);
    }

    @NotNull
    public String getUrl() {
        return myPluginDescriptor.getPluginResourcesPath(URL);
    }

    @Nullable
    @Override
    protected ModelAndView doHandle(@NotNull HttpServletRequest httpServletRequest, @NotNull HttpServletResponse httpServletResponse) throws Exception {
        String projectId = httpServletRequest.getParameter("projectId");
        String profileId = httpServletRequest.getParameter("profileId");
        String imageId = httpServletRequest.getParameter("imageId");
        if(StringUtil.isEmpty(imageId)) return null;

        final CloudClientEx client = myCloudManager.getClient(projectId, profileId);
        if (client == null) return null;

        CloudImage image = client.findImageById(imageId);
        if(image == null) return null;

        ModelAndView modelAndView = new ModelAndView(myPluginDescriptor.getPluginResourcesPath("deleteImageDialog.jsp"));
        modelAndView.getModelMap().put("image", image);
        modelAndView.getModelMap().put("instances", image.getInstances());
        return modelAndView;
    }
}
