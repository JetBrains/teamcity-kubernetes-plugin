package jetbrains.buildServer.clouds.kubernetes;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.clouds.CloudImage;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import jetbrains.buildServer.serverSide.executors.ExecutorServices;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 14.11.17.
 */
public class KubeBackgroundUpdaterImpl implements KubeBackgroundUpdater {
    private static final Logger LOG = Logger.getInstance(KubeBackgroundUpdaterImpl.class.getName());
    private static final String KUBE_POD_MONITORING_PERIOD = "teamcity.kube.pods.monitoring.period";

    private final Collection<KubeCloudClient> myRegisteredClients = new ArrayList<>();

    public KubeBackgroundUpdaterImpl(@NotNull ExecutorServices executorServices) {
        long delay = TeamCityProperties.getLong(KUBE_POD_MONITORING_PERIOD, 1);
        executorServices.getNormalExecutorService().scheduleWithFixedDelay(this::populateInstances, delay, delay, TimeUnit.MINUTES);
    }

    @Override
    public void registerClient(@NotNull KubeCloudClient client) {
        myRegisteredClients.add(client);
    }

    @Override
    public void unregisterClient(@NotNull KubeCloudClient client) {
        myRegisteredClients.remove(client);
    }

    private void populateInstances() {
        long populateInstancesStartTime = System.currentTimeMillis();
        for(KubeCloudClient client : myRegisteredClients){
            for(CloudImage image : client.getImages()){
                ((KubeCloudImage)image).populateInstances();
            }
        }
        LOG.debug("Populate ECS instances task finished in " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - populateInstancesStartTime) + " seconds");
    }
}
