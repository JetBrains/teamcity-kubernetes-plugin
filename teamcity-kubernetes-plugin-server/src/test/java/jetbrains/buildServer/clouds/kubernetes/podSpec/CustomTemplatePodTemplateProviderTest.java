package jetbrains.buildServer.clouds.kubernetes.podSpec;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClientException;
import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.clouds.CloudInstanceUserData;
import jetbrains.buildServer.clouds.kubernetes.KubeCloudClientParameters;
import jetbrains.buildServer.clouds.kubernetes.KubeCloudImage;
import jetbrains.buildServer.serverSide.ServerSettings;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 17.06.17.
 */
public class CustomTemplatePodTemplateProviderTest extends BaseTestCase {
    private PodTemplateProvider myPodTemplateProvider;
    private Mockery m;

    @BeforeMethod
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        m = new Mockery();
        ServerSettings serverSettings = m.mock(ServerSettings.class);
        m.checking(new Expectations(){{
            allowing(serverSettings).getServerUUID(); will(returnValue("server uuid"));
        }});
        myPodTemplateProvider = new CustomTemplatePodTemplateProvider(serverSettings);
    }

    @AfterMethod
    @Override
    protected void tearDown() throws Exception {
        m.assertIsSatisfied();
        super.tearDown();
    }

    @Test
    public void testGetPodTemplate_invalidJSON() throws Exception {
        CloudInstanceUserData instanceTag = createInstanceTag();
        KubeCloudClientParameters clientParams = m.mock(KubeCloudClientParameters.class);
        KubeCloudImage image = m.mock(KubeCloudImage.class);
        m.checking(new Expectations(){{
            allowing(image).getCustomPodTemplateSpec(); will(returnValue("foo"));
        }});
        assertExceptionThrown(() -> myPodTemplateProvider.getPodTemplate(instanceTag, image, clientParams), KubernetesClientException.class);
    }

    @Test
    public void testGetPodTemplate() throws Exception {
        CloudInstanceUserData instanceTag = createInstanceTag();
        KubeCloudClientParameters clientParams = m.mock(KubeCloudClientParameters.class);
        KubeCloudImage image = m.mock(KubeCloudImage.class);
        m.checking(new Expectations(){{
            allowing(clientParams).getNamespace(); will(returnValue("custom namespace"));
            allowing(image).getId(); will(returnValue("my image id"));
            allowing(image).getName(); will(returnValue("my image name"));
            allowing(image).getCustomPodTemplateSpec(); will(returnValue("{\n" +
                    "\t\t\"metadata\": {\n" +
                    "\t\t\t\"labels\": {\n" +
                    "\t\t\t\t\"app\": \"nginx\"\n" +
                    "\t\t\t}\n" +
                    "\t\t},\n" +
                    "\t\t\"spec\": {\n" +
                    "\t\t\t\"containers\": [\n" +
                    "\t\t\t\t{\n" +
                    "\t\t\t\t\t\"name\": \"nginx\",\n" +
                    "\t\t\t\t\t\"image\": \"nginx:1.7.9\",\n" +
                    "\t\t\t\t\t\"ports\": [\n" +
                    "\t\t\t\t\t\t{\n" +
                    "\t\t\t\t\t\t\t\"containerPort\": 80\n" +
                    "\t\t\t\t\t\t}\n" +
                    "\t\t\t\t\t]\n" +
                    "\t\t\t\t}\n" +
                    "\t\t\t]\n" +
                    "\t\t}\n" +
                    "}"));
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
        m.checking(new Expectations(){{
            allowing(clientParams).getNamespace(); will(returnValue("custom namespace"));
            allowing(image).getId(); will(returnValue("my image id"));
            allowing(image).getName(); will(returnValue("my image name"));
            allowing(image).getCustomPodTemplateSpec(); will(returnValue("{\n" +
                    "\t\t\"metadata\": {\n" +
                    "\t\t\t\"labels\": {\n" +
                    "\t\t\t\t\"app\": \"nginx\"\n" +
                    "\t\t\t}\n" +
                    "\t\t},\n" +
                    "\t\t\"spec\": {\n" +
                    "\t\t\t\"containers\": [\n" +
                    "\t\t\t\t{\n" +
                    "\t\t\t\t\t\"name\": \"nginx\",\n" +
                    "\t\t\t\t\t\"image\": \"nginx:1.7.9\",\n" +
                    "\t\t\t\t\t\"ports\": [\n" +
                    "\t\t\t\t\t\t{\n" +
                    "\t\t\t\t\t\t\t\"containerPort\": 80\n" +
                    "\t\t\t\t\t\t}\n" +
                    "\t\t\t\t\t]\n" +
                    "\t\t\t\t}\n" +
                    "\t\t\t]\n" +
                    "\t\t}\n" +
                    "}"));
        }});
        Pod podTemplate = myPodTemplateProvider.getPodTemplate(instanceTag, image, clientParams);
        for(Container container : podTemplate.getSpec().getContainers()){
            assertNotSame(container.getName(), "agent name");
        }
    }

    private CloudInstanceUserData createInstanceTag() {
        return new CloudInstanceUserData("agent name", "auth token", "server address", null, "profile id", "profile description", Collections.emptyMap());
    }
}