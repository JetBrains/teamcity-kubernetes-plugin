
package jetbrains.buildServer.clouds.kubernetes;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.clouds.CloudErrorInfo;
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
        long delay = TeamCityProperties.getLong(KUBE_POD_MONITORING_PERIOD, 60);
        executorServices.getNormalExecutorService().scheduleWithFixedDelay(this::populateInstances, delay, delay, TimeUnit.SECONDS);
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
        long populateInstancesStartTime = System.nanoTime();
        try {
            for (KubeCloudClient client : myRegisteredClients) {
                for (CloudImage image : client.getImages()) {
                    final KubeCloudImage kubeImage = (KubeCloudImage)image;
                    try {
                        kubeImage.populateInstances();
                        kubeImage.setErrorInfo(null);
                    } catch (Exception ex){
                        final String errorMessage = String.format("An error occurred while populating instances for %s(profile=%s)", kubeImage.getName(), client.getProfileId());
                        LOG.warnAndDebugDetails(errorMessage, ex);
                        kubeImage.setErrorInfo(new CloudErrorInfo(ex.getMessage(), ex.toString(), ex));
                    }
                }
            }
            LOG.debug("Populate instances task finished in " + TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - populateInstancesStartTime) + " seconds");
        } catch (Exception ex) {
            LOG.warnAndDebugDetails("An error occurred while populating kube instances", ex);
        }
    }
}