package ekoshkin.teamcity.clouds.kubernetes;

import ekoshkin.teamcity.clouds.kubernetes.connector.KubeApiConnector;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodStatusBuilder;
import jetbrains.buildServer.BaseTestCase;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Date;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 29.06.17.
 */
public class KubeCloudInstanceImplTest extends BaseTestCase {
    private Mockery m;
    private KubeApiConnector myApi;

    @BeforeMethod
    public void setUp() throws Exception {
        super.setUp();
        m = new Mockery();
        myApi = m.mock(KubeApiConnector.class);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testGetStartedTime() throws Exception {
        KubeCloudImage image = m.mock(KubeCloudImage.class);
        Pod pod = new Pod("1.0", "kind", new ObjectMeta(), new PodSpec(), new PodStatusBuilder().withStartTime("2017-06-12T22:59").build());
        KubeCloudInstanceImpl instance = new KubeCloudInstanceImpl(image, pod, myApi);
        assertEquals(new Date(), instance.getStartedTime());
    }
}