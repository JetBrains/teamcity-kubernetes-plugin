/*
 * Copyright 2000-2020 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.clouds.kubernetes;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.Pod;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
import jetbrains.buildServer.util.EventDispatcher;
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
  private BuildAgentPodTemplateProvidersImpl myPodTemplateProviders;

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
    myNameGenerator = new KubePodNameGeneratorImpl(myServerPaths, myExecutorServices, myEventDispatcher);
    myContainerProvider = new SimpleRunContainerProvider(myServerSettings, myNameGenerator);
    myPodTemplateProviders = new BuildAgentPodTemplateProvidersImpl(myServerSettings, (name, kubeClientParams) -> null, myNameGenerator);

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
      new EnvVar(KubeContainerEnvironment.IMAGE_NAME, "image1", null),
      new EnvVar(KubeContainerEnvironment.PROFILE_ID, "profile id", null),
      new EnvVar(KubeContainerEnvironment.INSTANCE_NAME, "prefix-1", null)
    );
  }

  public void store_idxes_on_shutdown(){
    final Map<String, String> imageParameters = createMap(CloudImageParameters.SOURCE_ID_FIELD, "image1",
                                              KubeParametersConstants.DOCKER_IMAGE, "jetbrains/teamcity-agent",
                                              KubeParametersConstants.AGENT_NAME_PREFIX, "prefix");
    final KubeCloudImage image = createImage(imageParameters);
    final KubePodNameGenerator newGenerator1 = new KubePodNameGeneratorImpl(myServerPaths, myExecutorServices, myEventDispatcher);

    then(newGenerator1.generateNewVmName(image)).isEqualTo("prefix-1");
    myEventDispatcher.getMulticaster().serverShutdown();
    final KubePodNameGenerator newGenerator2 = new KubePodNameGeneratorImpl(myServerPaths, myExecutorServices, myEventDispatcher);
    then(newGenerator2.generateNewVmName(image)).isEqualTo("prefix-2");
  }


  public void dont_generate_on_shutdown() throws InterruptedException {
    final Map<String, String> imageParameters = createMap(CloudImageParameters.SOURCE_ID_FIELD, "image1",
                                                          KubeParametersConstants.DOCKER_IMAGE, "jetbrains/teamcity-agent",
                                                          KubeParametersConstants.AGENT_NAME_PREFIX, "prefix");
    final KubeCloudImage image = createImage(imageParameters);
    final Thread[] thArray = new Thread[10];
    final Set<String> generatedNames = ConcurrentHashMap.newKeySet();
    for (int i=0; i<10; i++){
      final Thread th = new Thread(() -> {
        for (int j = 0; j < 50000; j++) {
          try {
            generatedNames.add(myNameGenerator.generateNewVmName(image));
          } catch (Exception ex) {
            break;
          }
        }

      });
      th.start();
      thArray[i] = th;
    }
    final long cur = System.currentTimeMillis();
    myEventDispatcher.getMulticaster().serverShutdown();
    then(System.currentTimeMillis() - cur).isLessThan(500l);
    for (int i=0; i<10; i++){
      thArray[i].join();
    }
    final KubePodNameGenerator newGenerator1 = new KubePodNameGeneratorImpl(myServerPaths, myExecutorServices, myEventDispatcher);
    int generatedSize = generatedNames.size();
    then(generatedSize).isLessThan(500000);
    then(newGenerator1.generateNewVmName(image)).isEqualTo("prefix-" + (generatedSize+1));
    System.out.println(generatedSize);



  }


  private Container createContainer(Map<String, String> imageParameters){
    final Pod podTemplate = createTemplate(imageParameters);
    return podTemplate.getSpec().getContainers().get(0);
  }

  private Pod createTemplate(Map<String, String> imageParameters){
    final CloudInstanceUserData instanceTag = createInstanceTag();
    final CloudClientParameters parameters = new CloudClientParametersImpl(createMap(), createSet());
    return myContainerProvider.getPodTemplate(instanceTag, createImage(imageParameters), new KubeCloudClientParametersImpl(parameters));
  }

  private KubeCloudImage createImage(Map<String, String> imageParameters){
    final CloudImageDataImpl imageData = new CloudImageDataImpl(imageParameters);
    return new KubeCloudImageImpl(new KubeCloudImageData(new CloudImageParametersImpl(imageData, PROJECT_ID, "image1")), new FakeKubeApiConnector(),
                                  myPodTemplateProviders);
  }

  private CloudInstanceUserData createInstanceTag() {
    return new CloudInstanceUserData("agent name", "auth token", "server address", null, "profile id", "profile description", Collections.emptyMap());
  }

}
