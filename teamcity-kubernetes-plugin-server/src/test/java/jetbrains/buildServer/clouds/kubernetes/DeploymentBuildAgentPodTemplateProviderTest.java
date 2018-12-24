package jetbrains.buildServer.clouds.kubernetes;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.api.model.extensions.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.extensions.DeploymentSpecBuilder;
import java.io.File;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.TempFiles;
import jetbrains.buildServer.clouds.CloudInstanceUserData;
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
public class DeploymentBuildAgentPodTemplateProviderTest extends BaseTestCase {
    private Mockery m;
    private BuildAgentPodTemplateProvider myPodTemplateProvider;
    private UnauthorizedAccessStrategy myAuthStrategy = new UnauthorizedAccessStrategy();
    private DeploymentContentProvider myDeploymentContentProvider;

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
        myPodTemplateProvider = new DeploymentBuildAgentPodTemplateProvider(
          serverSettings, myDeploymentContentProvider, new KubePodNameGenerator(serverPaths, executorServices, eventDispatcher));
    }

    @AfterMethod
    @Override
    protected void tearDown() throws Exception {
        m.assertIsSatisfied();
        super.tearDown();
    }

    @Test
    public void testGetPodTemplate_UnknownDeployment() throws Exception {
        CloudInstanceUserData instanceTag = createInstanceTag();
        KubeCloudClientParameters clientParams = m.mock(KubeCloudClientParameters.class);
        KubeCloudImage image = m.mock(KubeCloudImage.class);
        m.checking(new Expectations(){{
            allowing(image).getSourceDeploymentName(); will(returnValue("deploymentFoo"));
            allowing(myDeploymentContentProvider).findDeployment(with(any(String.class)), with(any(KubeCloudClientParameters.class))); will(returnValue(null));
        }});
        assertExceptionThrown(() -> myPodTemplateProvider.getPodTemplate(instanceTag, image, clientParams), KubeCloudException.class);
    }

    @Test
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
            allowing(image).getAgentName(with("agent name")); will(returnValue("prefix agent name"));
            allowing(myDeploymentContentProvider).findDeployment(with(any(String.class)), with(any(KubeCloudClientParameters.class))); will(returnValue(deployment));
        }});
        Pod podTemplate = myPodTemplateProvider.getPodTemplate(instanceTag, image, clientParams);
        assertNotNull(podTemplate);
        assertNotNull(podTemplate.getMetadata());
        assertNotNull(podTemplate.getSpec());
    }

    @Test
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
            allowing(image).getSourceDeploymentName(); will(returnValue("deploymentFoo"));
            allowing(image).getAgentName(with("agent name")); will(returnValue("prefix agent name"));
            allowing(myDeploymentContentProvider).findDeployment(with(any(String.class)), with(any(KubeCloudClientParameters.class))); will(returnValue(deployment));
        }});
        Pod podTemplate = myPodTemplateProvider.getPodTemplate(instanceTag, image, clientParams);
        for(Container container : podTemplate.getSpec().getContainers()){
            assertNotSame(container.getName(), "agent name");
        }
    }


    @Test
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
            allowing(image).getSourceDeploymentName(); will(returnValue("deploymentFoo"));
            allowing(image).getAgentName(with("agent name")); will(returnValue("prefix agent name"));
            allowing(myDeploymentContentProvider).findDeployment(with(any(String.class)), with(any(KubeCloudClientParameters.class))); will(returnValue(deployment));
        }});

        Pod podTemplate = myPodTemplateProvider.getPodTemplate(instanceTag, image, clientParams);

        assertNotEmpty(podTemplate.getSpec().getAdditionalProperties().keySet());
    }

    private CloudInstanceUserData createInstanceTag() {
        return new CloudInstanceUserData("agent name", "auth token", "server address", null, "profile id", "profile description", Collections.emptyMap());
    }
}