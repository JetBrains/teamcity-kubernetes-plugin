package jetbrains.buildServer.clouds.kubernetes;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.clouds.CloudClientParameters;
import jetbrains.buildServer.clouds.CloudImage;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnector;
import jetbrains.buildServer.clouds.kubernetes.podSpec.BuildAgentPodTemplateProviders;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants.PROFILE_INSTANCE_LIMIT;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 29.06.17.
 */
public class KubeCloudClientTest extends BaseTestCase {
    private Mockery m;
    private KubeApiConnector myApi;
    private BuildAgentPodTemplateProviders myPodTemplateProviders;

    @BeforeMethod
    public void setUp() throws Exception {
        super.setUp();
        m = new Mockery();
        myApi = m.mock(KubeApiConnector.class);
        myPodTemplateProviders = m.mock(BuildAgentPodTemplateProviders.class);
    }

    @NotNull
    private KubeCloudClient createClient(List<KubeCloudImage> images) {
        return createClient("defaultServerUuid", "defaultProfileId", images);
    }

    @NotNull
    private KubeCloudClient createClient(String serverUuid, String profileId, List<KubeCloudImage> images) {
        return new KubeCloudClient(serverUuid, profileId, myApi, images, new KubeCloudClientParametersImpl(new CloudClientParameters()), myPodTemplateProviders);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        m.assertIsSatisfied();
        super.tearDown();
    }

    @Test
    public void testIsInitialized() throws Exception {
        assertTrue(createClient(Collections.emptyList()).isInitialized());
    }

    @Test
    public void testCanStartNewInstance_UnknownImage() throws Exception {
        KubeCloudClient cloudClient = createClient(Collections.emptyList());
        CloudImage image = m.mock(KubeCloudImage.class);
        m.checking(new Expectations(){{
            allowing(image).getId(); will(returnValue("image-1-id"));
        }});
        assertFalse(cloudClient.canStartNewInstance(image));
    }

    @Test
    public void testCanStartNewInstance() throws Exception {
        KubeCloudImage image = m.mock(KubeCloudImage.class);
        m.checking(new Expectations(){{
            allowing(image).getName(); will(returnValue("image-1-name"));
            allowing(image).getId(); will(returnValue("image-1-id"));
            allowing(image).getInstanceCount(); will(returnValue(0));
            allowing(image).getInstanceLimit(); will(returnValue(0));
        }});
        List<KubeCloudImage> images = Collections.singletonList(image);
        KubeCloudClient cloudClient = createClient(images);
        assertTrue(cloudClient.canStartNewInstance(image));
    }

    @Test
    public void testCanStartNewInstance_ProfileLimit() throws Exception {
        KubeCloudImage image = m.mock(KubeCloudImage.class);
        m.checking(new Expectations(){{
            allowing(image).getName(); will(returnValue("image-1-name"));
            allowing(image).getId(); will(returnValue("image-1-id"));
            allowing(image).getInstanceCount(); will(returnValue(1));
        }});
        List<KubeCloudImage> images = Collections.singletonList(image);
        CloudClientParameters cloudClientParameters = new CloudClientParameters();
        cloudClientParameters.setParameter(PROFILE_INSTANCE_LIMIT, "1");
        KubeCloudClient cloudClient = createClient(images);
        assertFalse(cloudClient.canStartNewInstance(image));
    }

    @Test
    public void testCanStartNewInstance_ImageLimit() throws Exception {
        KubeCloudImage image = m.mock(KubeCloudImage.class);
        m.checking(new Expectations(){{
            allowing(image).getName(); will(returnValue("image-1-name"));
            allowing(image).getId(); will(returnValue("image-1-id"));
            allowing(image).getInstanceCount(); will(returnValue(1));
            allowing(image).getInstanceLimit(); will(returnValue(1));
        }});
        List<KubeCloudImage> images = Collections.singletonList(image);
        KubeCloudClient cloudClient = createClient(images);
        assertFalse(cloudClient.canStartNewInstance(image));
    }

    @Test
    public void testDuplicateImageName() throws Exception {
        KubeCloudImage image1 = m.mock(KubeCloudImage.class, "1");
        KubeCloudImage image2 = m.mock(KubeCloudImage.class, "2");
        createClient(Arrays.asList(image1, image2));
    }
}