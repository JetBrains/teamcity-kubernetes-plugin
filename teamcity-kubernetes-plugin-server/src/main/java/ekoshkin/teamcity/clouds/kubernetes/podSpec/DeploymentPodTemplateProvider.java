package ekoshkin.teamcity.clouds.kubernetes.podSpec;

import ekoshkin.teamcity.clouds.kubernetes.KubeCloudException;
import ekoshkin.teamcity.clouds.kubernetes.KubeCloudImage;
import ekoshkin.teamcity.clouds.kubernetes.connector.KubeApiConnection;
import ekoshkin.teamcity.clouds.kubernetes.connector.KubeApiConnector;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import jetbrains.buildServer.clouds.CloudInstanceUserData;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 15.06.17.
 */
public class DeploymentPodTemplateProvider implements PodTemplateProvider {
    private KubeApiConnector myKubeApiConnector;

    public DeploymentPodTemplateProvider(KubeApiConnector kubeApiConnector) {
        myKubeApiConnector = kubeApiConnector;
    }

    @NotNull
    @Override
    public String getId() {
        return "deployment-base";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Use pod template from deployment";
    }

    @Nullable
    @Override
    public String getDescription() {
        return null;
    }

    @NotNull
    @Override
    public Pod getPodTemplate(@NotNull CloudInstanceUserData cloudInstanceUserData, @NotNull KubeCloudImage kubeCloudImage, @NotNull KubeApiConnection kubeApiConnection) {
        String sourceDeploymentName = kubeCloudImage.getSourceDeploymentName();
        if(StringUtil.isEmpty(sourceDeploymentName))
            throw new KubeCloudException("Deployment name is not set in kubernetes cloud image " + kubeCloudImage.getId());

        Deployment sourceDeployment = myKubeApiConnector.getDeployment(sourceDeploymentName);
        if(sourceDeployment == null)
            throw new KubeCloudException("Can't find source deployment by name " + sourceDeploymentName);

        PodTemplateSpec podTemplateSpec = sourceDeployment.getSpec().getTemplate();
        //TODO: fill all required properties
        return new PodBuilder()
                .withSpec(podTemplateSpec.getSpec())
                .withMetadata(podTemplateSpec.getMetadata())
                .build();
    }
}
