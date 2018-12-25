package jetbrains.buildServer.clouds.kubernetes;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodTemplate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.TempFiles;
import jetbrains.buildServer.clouds.CloudClientParameters;
import jetbrains.buildServer.clouds.CloudImageParameters;
import jetbrains.buildServer.clouds.CloudInstanceUserData;
import jetbrains.buildServer.clouds.kubernetes.connector.FakeKubeApiConnector;
import jetbrains.buildServer.clouds.kubernetes.connector.ImagePullPolicy;
import jetbrains.buildServer.clouds.kubernetes.podSpec.BuildAgentPodTemplateProvidersImpl;
import jetbrains.buildServer.clouds.kubernetes.podSpec.SimpleRunContainerProvider;
import jetbrains.buildServer.clouds.server.impl.profile.CloudClientParametersImpl;
import jetbrains.buildServer.clouds.server.impl.profile.CloudImageDataImpl;
import jetbrains.buildServer.clouds.server.impl.profile.CloudImageParametersImpl;
import jetbrains.buildServer.serverSide.BuildServerListener;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.ServerSettings;
import jetbrains.buildServer.serverSide.executors.ExecutorServices;
import jetbrains.buildServer.serverSide.impl.ServerSettingsImpl;
import jetbrains.buildServer.serverSide.impl.executors.SimpleExecutorServices;
import jetbrains.buildServer.serverSide.impl.executors.TeamCityExecutorServicesImpl;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.BDDAssertions.then;

@Test
public class SimpleRunContainerProviderTest extends BaseTestCase {

  private static final String PROJECT_ID = "project123";

  private SimpleRunContainerProvider myContainerProvider;
  private ServerSettings myServerSettings;
  private KubePodNameGenerator myNameGenerator;
  private ServerPaths myServerPaths;
  private ExecutorServices myExecutorServices;
  private EventDispatcher<BuildServerListener> myEventDispatcher;

  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    myServerSettings = new ServerSettingsImpl(){
      @Nullable
      @Override
      public String getServerUUID() {
        return "SERVER-UUID";
      }
    };
    final TempFiles tempFiles = new TempFiles();
    myServerPaths = new ServerPaths(tempFiles.createTempDir());
    myExecutorServices = new SimpleExecutorServices();
    myEventDispatcher = EventDispatcher.create(BuildServerListener.class);
    myNameGenerator = new KubePodNameGenerator(myServerPaths, myExecutorServices, myEventDispatcher);
    myContainerProvider = new SimpleRunContainerProvider(myServerSettings, myNameGenerator);
  }

  public void check_generated_name(){
    final Map<String, String> params = createMap(CloudImageParameters.SOURCE_ID_FIELD, "image1",
                                                 KubeParametersConstants.DOCKER_IMAGE, "jetbrains/teamcity-agent");
    then(createContainer(params).getName()).isEqualTo("jetbrains-teamcity-agent-1");
    then(createContainer(params).getName()).isEqualTo("jetbrains-teamcity-agent-2");
  }

  public void check_generated_name_prefix(){
    final Container container = createContainer(createMap(CloudImageParameters.SOURCE_ID_FIELD, "image1",
                                                          KubeParametersConstants.DOCKER_IMAGE, "jetbrains/teamcity-agent",
                                                          KubeParametersConstants.AGENT_NAME_PREFIX, "prefix"));
    then(container).isNotNull();
    then(container.getName()).isEqualTo("prefix-1");
  }

  public void validate_all_parameters(){
    final Pod podTemplate = createTemplate(createMap(CloudImageParameters.SOURCE_ID_FIELD, "image1",
                                                          KubeParametersConstants.DOCKER_IMAGE, "jetbrains/teamcity-agent",
                                                          KubeParametersConstants.AGENT_NAME_PREFIX, "prefix"));
    then(podTemplate.getKind()).isEqualTo("Pod");
    final List<Container> containers = podTemplate.getSpec().getContainers();
    then(containers).hasSize(1);
    final Container container = containers.get(0);

    then(container).isNotNull();
    then(container.getImage()).isEqualTo("jetbrains/teamcity-agent");
    then(container.getCommand()).isEmpty();
    then(container.getImagePullPolicy()).isEqualTo(ImagePullPolicy.IfNotPresent.getName());
    then(container.getWorkingDir()).isNull();
    then(container.getEnv()).hasSize(6).containsOnly(
      new EnvVar(KubeContainerEnvironment.SERVER_URL, "server address", null),
      new EnvVar(KubeContainerEnvironment.SERVER_UUID, "SERVER-UUID", null),
      new EnvVar(KubeContainerEnvironment.OFFICIAL_IMAGE_SERVER_URL, "server address", null),
      new EnvVar(KubeContainerEnvironment.IMAGE_ID, "image1", null),
      new EnvVar(KubeContainerEnvironment.PROFILE_ID, "profile id", null),
      new EnvVar(KubeContainerEnvironment.INSTANCE_NAME, "prefix-1", null)
    );
  }



  private Container createContainer(Map<String, String> imageParameters){
    final Pod podTemplate = createTemplate(imageParameters);
    return podTemplate.getSpec().getContainers().get(0);
  }

  private Pod createTemplate(Map<String, String> imageParameters){
    final CloudInstanceUserData instanceTag = createInstanceTag();
    final BuildAgentPodTemplateProvidersImpl podTemplateProviders = new BuildAgentPodTemplateProvidersImpl(
      myServerSettings, (name, kubeClientParams) -> null, myNameGenerator);
    final KubeDataCacheImpl cache = new KubeDataCacheImpl();
    final CloudClientParameters parameters = new CloudClientParametersImpl(createMap(), createSet());
    final CloudImageDataImpl imageData = new CloudImageDataImpl(imageParameters);
    KubeCloudImage image = new KubeCloudImageImpl(new KubeCloudImageData(new CloudImageParametersImpl(imageData, PROJECT_ID, "image1")), new FakeKubeApiConnector(), cache, podTemplateProviders);
    return myContainerProvider.getPodTemplate(instanceTag, image, new KubeCloudClientParametersImpl(parameters));
  }

  private CloudInstanceUserData createInstanceTag() {
    return new CloudInstanceUserData("agent name", "auth token", "server address", null, "profile id", "profile description", Collections.emptyMap());
  }

}
