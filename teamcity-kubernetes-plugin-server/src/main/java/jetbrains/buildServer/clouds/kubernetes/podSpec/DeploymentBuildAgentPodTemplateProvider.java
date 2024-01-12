
package jetbrains.buildServer.clouds.kubernetes.podSpec;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import jetbrains.buildServer.clouds.CloudInstanceUserData;
import jetbrains.buildServer.clouds.kubernetes.*;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnector;
import jetbrains.buildServer.serverSide.ServerSettings;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 15.06.17.
 */
public class DeploymentBuildAgentPodTemplateProvider extends AbstractPodTemplateProvider{
  public static final String ID = "deployment-base";

  private final ServerSettings myServerSettings;

  public DeploymentBuildAgentPodTemplateProvider(@NotNull ServerSettings serverSettings) {
    myServerSettings = serverSettings;
  }

  @NotNull
  @Override
  public String getId() {
    return ID;
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
  public Pod getPodTemplate(@NotNull String instanceName,
                            @NotNull CloudInstanceUserData cloudInstanceUserData,
                            @NotNull KubeCloudImage kubeCloudImage,
                            @NotNull KubeApiConnector apiConnector) {
    String sourceDeploymentName = kubeCloudImage.getSourceDeploymentName();
    if (StringUtil.isEmpty(sourceDeploymentName)) {
      throw new KubeCloudException("Deployment name is not set in kubernetes cloud image " + kubeCloudImage.getId());
    }


    Deployment sourceDeployment = apiConnector.getDeployment(sourceDeploymentName);
    if (sourceDeployment == null) {
      throw new KubeCloudException("Can't find source deployment by name " + sourceDeploymentName);
    }

    //final String agentNameProvided = cloudInstanceUserData.getAgentName();
    //final String instanceName = StringUtil.isEmpty(agentNameProvided) ? sourceDeploymentName + "-" + UUID.randomUUID().toString() : agentNameProvided;

    return patchedPodTemplateSpec(sourceDeployment.getSpec().getTemplate(),
                                  instanceName,
                                  apiConnector.getNamespace(),
                                  myServerSettings.getServerUUID(),
                                  kubeCloudImage.getId(),
                                  cloudInstanceUserData
    );
    }
}