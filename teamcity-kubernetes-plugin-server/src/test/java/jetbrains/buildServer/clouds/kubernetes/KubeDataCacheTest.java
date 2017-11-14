package jetbrains.buildServer.clouds.kubernetes;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.clouds.InstanceStatus;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.concurrent.Callable;

import static jetbrains.buildServer.clouds.kubernetes.KubeDataCacheImpl.CACHE_EXPIRATION_TIMEOUT_PROPERTY;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 24.10.17.
 */
public class KubeDataCacheTest extends BaseTestCase {
    private KubeDataCache myCache;

    @BeforeMethod
    public void setUp() throws Exception {
        super.setUp();
        setInternalProperty(CACHE_EXPIRATION_TIMEOUT_PROPERTY, String.valueOf(50));
        myCache = new KubeDataCacheImpl();
    }

    @Test
    public void testGetInstanceStartedTimeShouldCache() throws Exception {
        final int[] invocationCount = {0};
        Date date = new Date();
        Callable<Date> resolver = () -> {
            invocationCount[0]++;
            return date;
        };
        assertEquals(date, myCache.getInstanceStartedTime("foo", resolver));
        assertEquals(date, myCache.getInstanceStartedTime("foo", resolver));
        assertEquals(date, myCache.getInstanceStartedTime("boo", resolver));
        assertEquals(2, invocationCount[0]);
    }

    @Test
    public void testGetInstanceStartedTimeShouldExpires() throws Exception {
        final int[] invocationCount = {0};
        Callable<Date> resolver = () -> {
            invocationCount[0]++;
            return new Date();
        };
        myCache.getInstanceStartedTime("foo", resolver);
        myCache.getInstanceStartedTime("foo", resolver);
        myCache.getInstanceStartedTime("foo", resolver);
        assertEquals(1, invocationCount[0]);
        Thread.sleep(100);
        myCache.getInstanceStartedTime("foo", resolver);
        myCache.getInstanceStartedTime("foo", resolver);
        assertEquals(2, invocationCount[0]);
        Thread.sleep(100);
        myCache.getInstanceStartedTime("foo", resolver);
        assertEquals(3, invocationCount[0]);
    }

    @Test
    public void testGetInstanceStatusShouldCache() throws Exception {
        final int[] invocationCount = {0};
        Callable<InstanceStatus> resolver = () -> {
                invocationCount[0]++;
                return InstanceStatus.RUNNING;
        };
        assertEquals(InstanceStatus.RUNNING, myCache.getInstanceStatus("foo", resolver));
        assertEquals(InstanceStatus.RUNNING, myCache.getInstanceStatus("foo", resolver));
        assertEquals(1, invocationCount[0]);
        assertEquals(InstanceStatus.RUNNING, myCache.getInstanceStatus("boo", resolver));
        assertEquals(2, invocationCount[0]);
        myCache.cleanInstanceStatus("foo");
        assertEquals(InstanceStatus.RUNNING, myCache.getInstanceStatus("foo", resolver));
        assertEquals(3, invocationCount[0]);
    }

    @Test
    public void testGetInstanceStatusShouldExpires() throws Exception {
        final int[] invocationCount = {0};
        Callable<InstanceStatus> resolver = () -> {
            invocationCount[0]++;
            switch (invocationCount[0]){
                case 1: return InstanceStatus.STARTING;
                case 2: return InstanceStatus.RUNNING;
                case 3: return InstanceStatus.STOPPING;
                case 4: return InstanceStatus.STOPPED;
                default: return InstanceStatus.UNKNOWN;
            }
        };
        assertEquals(InstanceStatus.STARTING, myCache.getInstanceStatus("foo", resolver));
        assertEquals(InstanceStatus.STARTING, myCache.getInstanceStatus("foo", resolver));
        assertEquals(InstanceStatus.STARTING, myCache.getInstanceStatus("foo", resolver));
        Thread.sleep(100);
        assertEquals(InstanceStatus.RUNNING, myCache.getInstanceStatus("foo", resolver));
        assertEquals(InstanceStatus.RUNNING, myCache.getInstanceStatus("foo", resolver));
        Thread.sleep(100);
        assertEquals(InstanceStatus.STOPPING, myCache.getInstanceStatus("foo", resolver));
        Thread.sleep(100);
        assertEquals(InstanceStatus.STOPPED, myCache.getInstanceStatus("foo", resolver));
    }
}