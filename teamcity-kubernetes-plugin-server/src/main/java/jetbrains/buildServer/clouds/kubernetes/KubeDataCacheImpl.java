package jetbrains.buildServer.clouds.kubernetes;

import java.util.concurrent.ConcurrentMap;
import jetbrains.buildServer.clouds.InstanceStatus;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 24.10.17.
 */
public class KubeDataCacheImpl implements KubeDataCache {
    public static String CACHE_EXPIRATION_TIMEOUT_PROPERTY = "teamcity.kube.cache.expirationTimeout";

    private final ConcurrentMap<String, CacheEntry<InstanceStatus>> myInstanceStatusCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, CacheEntry<Date>> myInstanceStartedTimeCache = new ConcurrentHashMap<>();

    @NotNull
    @Override
    public Date getInstanceStartedTime(@NotNull String instanceId, @NotNull Callable<Date> resolver) throws Exception {
        long now = System.currentTimeMillis();
        synchronized (myInstanceStartedTimeCache) {
            CacheEntry<Date> cacheEntry = myInstanceStartedTimeCache.get(instanceId);
            if (cacheEntry == null || (now - cacheEntry.getTimestamp()) > TeamCityProperties.getInteger(CACHE_EXPIRATION_TIMEOUT_PROPERTY, 10 * 1000)) {
                myInstanceStartedTimeCache.put(instanceId, new CacheEntry<>(now, resolver.call()));
            }
        }
        return myInstanceStartedTimeCache.get(instanceId).getData();
    }

    @NotNull
    @Override
    public InstanceStatus getInstanceStatus(@NotNull String instanceId, @NotNull Callable<InstanceStatus> resolver) throws Exception {
        long now = System.currentTimeMillis();
        synchronized (myInstanceStatusCache) {
            CacheEntry<InstanceStatus> cacheEntry = myInstanceStatusCache.get(instanceId);
            if (cacheEntry == null || (now - cacheEntry.getTimestamp()) > TeamCityProperties.getInteger(CACHE_EXPIRATION_TIMEOUT_PROPERTY, 10 * 1000)) {
                myInstanceStatusCache.put(instanceId, new CacheEntry<>(now, resolver.call()));
            }
        }
        return myInstanceStatusCache.get(instanceId).getData();
    }

    @Override
    public void cleanInstanceStatus(@NotNull String instanceId) {
        myInstanceStatusCache.remove(instanceId);
    }

    @Override
    public void invalidate() {
        myInstanceStatusCache.clear();
        myInstanceStartedTimeCache.clear();
    }

    private class CacheEntry<T> {
        private long myTimestamp;
        private T myData;

        private CacheEntry(long timestamp, @NotNull T data) {
            myTimestamp = timestamp;
            myData = data;
        }

        long getTimestamp() {
            return myTimestamp;
        }

        T getData() {
            return myData;
        }
    }
}
