package ekoshkin.teamcity.clouds.kubernetes.podSpec;

import ekoshkin.teamcity.clouds.kubernetes.KubeCloudClientParameters;
import ekoshkin.teamcity.clouds.kubernetes.KubeCloudException;
import ekoshkin.teamcity.clouds.kubernetes.KubeCloudImage;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import io.fabric8.kubernetes.client.utils.Serialization;
import jetbrains.buildServer.clouds.CloudInstanceUserData;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 15.06.17.
 */
public class CustomTemplatePodTemplateProvider implements PodTemplateProvider {
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
    public Pod getPodTemplate(@NotNull CloudInstanceUserData cloudInstanceUserData, @NotNull KubeCloudImage kubeCloudImage, @NotNull KubeCloudClientParameters clientParameters) {
        String customPodTemplateSpecContent = kubeCloudImage.getCustomPodTemplateSpec();
        if(StringUtil.isEmpty(customPodTemplateSpecContent))
            throw new KubeCloudException("Custom pod template spec is not specified for image " + kubeCloudImage.getId());

        PodTemplateSpec cuPodTemplateSpec = Serialization.unmarshal(new ByteArrayInputStream(customPodTemplateSpecContent.getBytes()), PodTemplateSpec.class);
        //TODO: fill all properties
        return new PodBuilder()
                .withMetadata(cuPodTemplateSpec.getMetadata())
                .withSpec(cuPodTemplateSpec.getSpec())
                .build();
    }
}
