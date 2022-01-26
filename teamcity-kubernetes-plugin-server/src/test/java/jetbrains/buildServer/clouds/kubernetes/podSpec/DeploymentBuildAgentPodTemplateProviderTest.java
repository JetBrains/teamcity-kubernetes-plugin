/*
 * Copyright 2000-2022 JetBrains s.r.o.
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

package jetbrains.buildServer.clouds.kubernetes.podSpec;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import java.io.File;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentSpecBuilder;
import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.TempFiles;
import jetbrains.buildServer.clouds.CloudInstanceUserData;
import jetbrains.buildServer.clouds.kubernetes.*;
import jetbrains.buildServer.clouds.kubernetes.auth.KubeAuthStrategyProvider;
import jetbrains.buildServer.clouds.kubernetes.auth.UnauthorizedAccessStrategy;
import jetbrains.buildServer.clouds.kubernetes.podSpec.BuildAgentPodTemplateProvider;
import jetbrains.buildServer.clouds.kubernetes.podSpec.DeploymentBuildAgentPodTemplateProvider;
import jetbrains.buildServer.clouds.kubernetes.podSpec.DeploymentContentProvider;
import jetbrains.buildServer.serverSide.BuildServerListener;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.ServerSettings;
import jetbrains.buildServer.serverSide.executors.ExecutorServices;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.EventDispatcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 17.06.17.
 */
@Test
public class DeploymentBuildAgentPodTemplateProviderTest extends BaseTestCase {
    private Mockery m;
    private BuildAgentPodTemplateProvider myPodTemplateProvider;
    private UnauthorizedAccessStrategy myAuthStrategy = new UnauthorizedAccessStrategy();
    private DeploymentContentProvider myDeploymentContentProvider;
    private KubePodNameGenerator myNameGenerator;

    @BeforeMethod
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        m = new Mockery();
        ServerSettings serverSettings = m.mock(ServerSettings.class);
        KubeAuthStrategyProvider authStrategies = m.mock(KubeAuthStrategyProvider.class);
        myDeploymentContentProvider = m.mock(DeploymentContentProvider.class);
        final ExecutorServices executorServices = m.mock(ExecutorServices.class);
        m.checking(new Expectations(){{
            allowing(serverSettings).getServerUUID(); will(returnValue("server uuid"));
            allowing(authStrategies).get(with(UnauthorizedAccessStrategy.ID)); will(returnValue(myAuthStrategy));
            ScheduledExecutorService ses = new ScheduledThreadPoolExecutor(1);
            allowing(executorServices).getNormalExecutorService(); will(returnValue(ses));
        }});
        TempFiles tempFiles = new TempFiles();
        final ServerPaths serverPaths = new ServerPaths(tempFiles.createTempDir());
        final EventDispatcher<BuildServerListener> eventDispatcher = EventDispatcher.create(BuildServerListener.class);
        myNameGenerator = new KubePodNameGeneratorImpl(serverPaths, executorServices, eventDispatcher);
        myPodTemplateProvider = new DeploymentBuildAgentPodTemplateProvider(serverSettings, myDeploymentContentProvider);
    }

    @AfterMethod
    @Override
    protected void tearDown() throws Exception {
        m.assertIsSatisfied();
        super.tearDown();
    }

    public void testGetPodTemplate_UnknownDeployment() throws Exception {
        CloudInstanceUserData instanceTag = createInstanceTag();
        KubeCloudClientParameters clientParams = m.mock(KubeCloudClientParameters.class);
        KubeCloudImage image = m.mock(KubeCloudImage.class);
        m.checking(new Expectations(){{
            allowing(image).getSourceDeploymentName(); will(returnValue("deploymentFoo"));
            allowing(image).getAgentNamePrefix(); will(returnValue("agent-name-prefix"));
            allowing(image).findInstanceById(with("agent-name-prefix-1")); will(returnValue(null));
            allowing(myDeploymentContentProvider).findDeployment(with(any(String.class)), with(any(KubeCloudClientParameters.class))); will(returnValue(null));
        }});
        assertExceptionThrown(() -> myPodTemplateProvider.getPodTemplate(myNameGenerator.generateNewVmName(image), instanceTag, image, clientParams), KubeCloudException.class);
    }

    public void testGetPodTemplate() throws Exception {
        CloudInstanceUserData instanceTag = createInstanceTag();
        KubeCloudClientParameters clientParams = m.mock(KubeCloudClientParameters.class);
        KubeCloudImage image = m.mock(KubeCloudImage.class);
        Deployment deployment = new DeploymentBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withLabels(CollectionsUtil.asMap("app", "nginx"))
                        .build())
                .withSpec(new DeploymentSpecBuilder()
                        .withTemplate(new PodTemplateSpecBuilder()
                                .withMetadata(new ObjectMeta())
                                .withSpec(new PodSpecBuilder()
                                        .withContainers(new ContainerBuilder()
                                                .withName("nginx")
                                                .withImage("nginx:1.7.9")
                                                .withPorts(new ContainerPortBuilder()
                                                        .withHostPort(80)
                                                        .build())
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();
        m.checking(new Expectations(){{
            allowing(clientParams).getNamespace(); will(returnValue("custom namespace"));
            allowing(clientParams).getAuthStrategy(); will(returnValue(UnauthorizedAccessStrategy.ID));
            allowing(image).getId(); will(returnValue("my image id"));
            allowing(image).getName(); will(returnValue("my image name"));
            allowing(image).getSourceDeploymentName(); will(returnValue("deploymentFoo"));
            allowing(image).getAgentNamePrefix(); will(returnValue("agent-name-prefix"));
            allowing(image).findInstanceById(with("agent-name-prefix-1")); will(returnValue(null));
            allowing(image).getAgentName(with("agent name")); will(returnValue("prefix agent name"));
            allowing(myDeploymentContentProvider).findDeployment(with(any(String.class)), with(any(KubeCloudClientParameters.class))); will(returnValue(deployment));
        }});
        Pod podTemplate = myPodTemplateProvider.getPodTemplate(myNameGenerator.generateNewVmName(image), instanceTag, image, clientParams);
        assertNotNull(podTemplate);
        assertNotNull(podTemplate.getMetadata());
        assertNotNull(podTemplate.getSpec());
    }

    public void testShouldNotsetContainerName(){
        CloudInstanceUserData instanceTag = createInstanceTag();
        KubeCloudClientParameters clientParams = m.mock(KubeCloudClientParameters.class);
        KubeCloudImage image = m.mock(KubeCloudImage.class);
        Deployment deployment = new DeploymentBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withLabels(CollectionsUtil.asMap("app", "nginx"))
                        .build())
                .withSpec(new DeploymentSpecBuilder()
                        .withTemplate(new PodTemplateSpecBuilder()
                                .withMetadata(new ObjectMeta())
                                .withSpec(new PodSpecBuilder()
                                        .withContainers(new ContainerBuilder()
                                                .withName("nginx")
                                                .withImage("nginx:1.7.9")
                                                .withPorts(new ContainerPortBuilder()
                                                        .withHostPort(80)
                                                        .build())
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();
        m.checking(new Expectations(){{
            allowing(clientParams).getNamespace(); will(returnValue("custom namespace"));
            allowing(image).getId(); will(returnValue("my image id"));
            allowing(image).getName(); will(returnValue("my image name"));
            allowing(image).getAgentNamePrefix(); will(returnValue("agent-name-prefix"));
            allowing(image).findInstanceById(with("agent-name-prefix-1")); will(returnValue(null));
            allowing(image).getSourceDeploymentName(); will(returnValue("deploymentFoo"));
            allowing(image).getAgentName(with("agent name")); will(returnValue("prefix agent name"));
            allowing(myDeploymentContentProvider).findDeployment(with(any(String.class)), with(any(KubeCloudClientParameters.class))); will(returnValue(deployment));
        }});
        Pod podTemplate = myPodTemplateProvider.getPodTemplate(myNameGenerator.generateNewVmName(image), instanceTag, image, clientParams);
        for(Container container : podTemplate.getSpec().getContainers()){
            assertNotSame(container.getName(), "agent name");
        }
    }

    public void testDoNotLoseSpecAdditionalProperties() throws Exception {
        CloudInstanceUserData instanceTag = createInstanceTag();
        KubeCloudClientParameters clientParams = m.mock(KubeCloudClientParameters.class);
        KubeCloudImage image = m.mock(KubeCloudImage.class);

        Deployment deployment = new DeploymentBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withLabels(CollectionsUtil.asMap("app", "nginx"))
                        .build())
                .withSpec(new DeploymentSpecBuilder()
                        .withTemplate(new PodTemplateSpecBuilder()
                                .withMetadata(new ObjectMeta())
                                .withSpec(new PodSpecBuilder()
                                        .withContainers(new ContainerBuilder()
                                                .withName("nginx")
                                                .withImage("nginx:1.7.9")
                                                .withPorts(new ContainerPortBuilder()
                                                        .withHostPort(80)
                                                        .build())
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();

        deployment.getSpec().getTemplate().getSpec().setAdditionalProperty("affinity", "some value");

        m.checking(new Expectations(){{
            allowing(clientParams).getNamespace(); will(returnValue("custom namespace"));
            allowing(image).getId(); will(returnValue("my image id"));
            allowing(image).getName(); will(returnValue("my image name"));
            allowing(image).getAgentNamePrefix(); will(returnValue("agent-name-prefix"));
            allowing(image).findInstanceById(with("agent-name-prefix-1")); will(returnValue(null));
            allowing(image).getSourceDeploymentName(); will(returnValue("deploymentFoo"));
            allowing(image).getAgentName(with("agent name")); will(returnValue("prefix agent name"));
            allowing(myDeploymentContentProvider).findDeployment(with(any(String.class)), with(any(KubeCloudClientParameters.class))); will(returnValue(deployment));
        }});

        Pod podTemplate = myPodTemplateProvider.getPodTemplate(myNameGenerator.generateNewVmName(image), instanceTag, image, clientParams);

        assertNotEmpty(podTemplate.getSpec().getAdditionalProperties().keySet());
    }

    private CloudInstanceUserData createInstanceTag() {
        return new CloudInstanceUserData("agent name", "auth token", "server address", null, "profile id", "profile description", Collections.emptyMap());
    }
}