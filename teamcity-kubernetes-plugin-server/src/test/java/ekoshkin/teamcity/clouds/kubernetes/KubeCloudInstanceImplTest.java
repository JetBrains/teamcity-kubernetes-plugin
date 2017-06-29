package ekoshkin.teamcity.clouds.kubernetes;

import ekoshkin.teamcity.clouds.kubernetes.connector.KubeApiConnector;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodStatusBuilder;
import jetbrains.buildServer.BaseTestCase;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

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
        m.assertIsSatisfied();
        super.tearDown();
    }

    @Test
    public void testGetStartedTime() throws Exception {
        KubeCloudImage image = m.mock(KubeCloudImage.class);
        Pod pod = new Pod("1.0", "kind", new ObjectMeta(), new PodSpec(), new PodStatusBuilder().withStartTime("2017-06-12T22:59Z").build());
        m.checking(new Expectations(){{
            allowing(myApi).getPodStatus(with(pod)); will(returnValue(pod.getStatus()));
        }});
        KubeCloudInstanceImpl instance = new KubeCloudInstanceImpl(image, pod, myApi);
        Date startedTime = instance.getStartedTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        assertEquals("2017-06-12T22:59Z", dateFormat.format(startedTime));
    }
}