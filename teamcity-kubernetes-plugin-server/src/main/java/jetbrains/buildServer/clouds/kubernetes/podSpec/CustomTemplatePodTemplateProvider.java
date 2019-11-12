package jetbrains.buildServer.clouds.kubernetes.podSpec;

import com.intellij.openapi.util.Pair;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.utils.Serialization;
import java.io.File;
import jetbrains.buildServer.Used;
import jetbrains.buildServer.clouds.CloudInstanceUserData;
import jetbrains.buildServer.clouds.kubernetes.*;
import jetbrains.buildServer.serverSide.ServerSettings;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.util.*;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 15.06.17.
 */
public class CustomTemplatePodTemplateProvider extends AbstractPodTemplateProvider {
    private final ServerSettings myServerSettings;
    private KubePodNameGenerator myPodNameGenerator;

    public CustomTemplatePodTemplateProvider(ServerSettings serverSettings, final KubePodNameGenerator podNameGenerator) {
        myServerSettings = serverSettings;
        myPodNameGenerator = podNameGenerator;
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

    @Nullable
    @Override
    public String getDescription() {
        return null;
    }

    @NotNull
    @Override
    public Pod getPodTemplate(@NotNull CloudInstanceUserData cloudInstanceUserData, @NotNull KubeCloudImage kubeCloudImage, @NotNull KubeCloudClientParameters kubeClientParams) {
        final String instanceName = myPodNameGenerator.generateNewVmName(kubeCloudImage);

        String spec = kubeCloudImage.getCustomPodTemplateSpec();
        return getPodTemplateInternal(cloudInstanceUserData, kubeCloudImage.getId(), kubeClientParams.getNamespace(), instanceName, spec);
    }

    @Used("tests")
    /* package local for tests */ Pod getPodTemplateInternal(@NotNull final CloudInstanceUserData cloudInstanceUserData,
                                      @NotNull final String imageId,
                                      @NotNull final String namespace,
                                      final String instanceName,
                                      String spec) {
        spec = spec.replaceAll("%instance\\.id%", instanceName);

        if (StringUtil.isEmpty(spec)) {
            throw new KubeCloudException("Custom pod template spec is not specified for image " + imageId);
        }

        final PodTemplateSpec podTemplateSpec = Serialization.unmarshal(
          new ByteArrayInputStream(spec.getBytes()),
          PodTemplateSpec.class
        );

        return patchedPodTemplateSpec(podTemplateSpec,
                                      instanceName,
                                      namespace,
                                      myServerSettings.getServerUUID(),
                                      imageId,
                                      cloudInstanceUserData);
    }
}
