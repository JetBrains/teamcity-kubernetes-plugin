
package jetbrains.buildServer.clouds.kubernetes.podSpec;

import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import io.fabric8.kubernetes.client.utils.Serialization;
import jetbrains.buildServer.Used;
import jetbrains.buildServer.clouds.CloudInstanceUserData;
import jetbrains.buildServer.clouds.kubernetes.KubeCloudImage;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnector;
import jetbrains.buildServer.serverSide.ServerSettings;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 15.06.17.
 */
public class CustomTemplatePodTemplateProvider extends AbstractPodTemplateProvider {
    private final ServerSettings myServerSettings;

    public CustomTemplatePodTemplateProvider(ServerSettings serverSettings) {
        myServerSettings = serverSettings;
    }

    @NotNull
    @Override
    public String getId() {
        return "custom-pod-template";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Use custom pod template";
    }

    @NotNull
    @Override
    public Pod getPodTemplate(@NotNull String kubeInstanceName,
                              @NotNull CloudInstanceUserData cloudInstanceUserData,
                              @NotNull KubeCloudImage kubeCloudImage,
                              @NotNull KubeApiConnector apiConnector) {
        String podTemplate = kubeCloudImage.getPodTemplate();
        if (StringUtil.isEmpty(podTemplate)) {
            throw new IllegalStateException("pod template is null or empty in cloud image " + kubeCloudImage.getId());
        }
        return getPodTemplateInternal(cloudInstanceUserData, kubeCloudImage.getId(), apiConnector.getNamespace(), kubeInstanceName, podTemplate);
    }

    /* package local for tests */
    @Used("tests")
    Pod getPodTemplateInternal(
            @NotNull final CloudInstanceUserData cloudInstanceUserData,
            @NotNull final String imageId,
            @NotNull final String namespace,
            @NotNull final String instanceName,
            @NotNull String spec
    ) {
        spec = spec.replaceAll("%instance\\.id%", instanceName);

        final PodTemplateSpec podTemplateSpec = Serialization.unmarshal(spec, PodTemplateSpec.class);

        return patchedPodTemplateSpec(
                podTemplateSpec,
                instanceName,
                namespace,
                myServerSettings.getServerUUID(),
                imageId,
                cloudInstanceUserData
        );
    }

    @Nullable
    @Override
    public PersistentVolumeClaim getPVC(@NotNull final String instanceName,
                                        @NotNull final KubeCloudImage kubeCloudImage) {
        String pvcTemplate = kubeCloudImage.getPVCTemplate();
        if (StringUtil.isEmpty(pvcTemplate)){
            return null;
        }
        pvcTemplate = pvcTemplate.replaceAll("%instance\\.id%", instanceName);
        //noinspection UnnecessaryLocalVariable
        final PersistentVolumeClaim pvc = Serialization.unmarshal(pvcTemplate, PersistentVolumeClaim.class);

        return pvc;
    }
}