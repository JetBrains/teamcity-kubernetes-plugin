package jetbrains.buildServer.clouds.kubernetes.podSpec;

import com.intellij.openapi.util.Pair;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import java.io.File;
import jetbrains.buildServer.clouds.CloudInstanceUserData;
import jetbrains.buildServer.clouds.kubernetes.*;
import jetbrains.buildServer.serverSide.ServerSettings;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 15.06.17.
 */
public class DeploymentBuildAgentPodTemplateProvider extends AbstractPodTemplateProvider{
  public static final String ID = "deployment-base";

  private final ServerSettings myServerSettings;
  private final DeploymentContentProvider myDeploymentContentProvider;
  private KubePodNameGenerator myPodNameGenerator;

  public DeploymentBuildAgentPodTemplateProvider(@NotNull ServerSettings serverSettings,
                                                 @NotNull DeploymentContentProvider deploymentContentProvider,
                                                 @NotNull final KubePodNameGenerator podNameGenerator) {
    myServerSettings = serverSettings;
    myDeploymentContentProvider = deploymentContentProvider;
    myPodNameGenerator = podNameGenerator;
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
  public Pod getPodTemplate(@NotNull CloudInstanceUserData cloudInstanceUserData,
                            @NotNull KubeCloudImage kubeCloudImage,
                            @NotNull KubeCloudClientParameters kubeClientParams) {
    String sourceDeploymentName = kubeCloudImage.getSourceDeploymentName();
    if (StringUtil.isEmpty(sourceDeploymentName)) {
      throw new KubeCloudException("Deployment name is not set in kubernetes cloud image " + kubeCloudImage.getId());
    }

    Deployment sourceDeployment = myDeploymentContentProvider.findDeployment(sourceDeploymentName, kubeClientParams);
    if (sourceDeployment == null) {
      throw new KubeCloudException("Can't find source deployment by name " + sourceDeploymentName);
    }

    final String agentNameProvided = cloudInstanceUserData.getAgentName();
    final String instanceName = StringUtil.isEmpty(agentNameProvided) ? sourceDeploymentName + "-" + UUID.randomUUID().toString() : agentNameProvided;

    return patchedPodTemplateSpec(sourceDeployment.getSpec().getTemplate(),
                                  instanceName,
                                  kubeClientParams.getNamespace(),
                                  myServerSettings.getServerUUID(),
                                  cloudInstanceUserData.getProfileId(),
                                  kubeCloudImage.getId(),
                                  cloudInstanceUserData.getServerAddress()
    );
    }
}
