
package jetbrains.buildServer.clouds.kubernetes;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.clouds.CloudErrorInfo;
import jetbrains.buildServer.clouds.CloudImage;
import jetbrains.buildServer.clouds.CloudInstance;
import jetbrains.buildServer.clouds.InstanceStatus;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.BuildServerListener;
import jetbrains.buildServer.serverSide.SBuildAgent;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import jetbrains.buildServer.serverSide.executors.ExecutorServices;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 14.11.17.
 */
public class KubeBackgroundUpdaterImpl implements KubeBackgroundUpdater {
    private static final Logger LOG = Logger.getInstance(KubeBackgroundUpdaterImpl.class.getName());
    private static final String KUBE_POD_MONITORING_PERIOD = "teamcity.kube.pods.monitoring.period";

    private final Collection<KubeCloudClient> myRegisteredClients = new CopyOnWriteArrayList<>();

    public KubeBackgroundUpdaterImpl(@NotNull ExecutorServices executorServices,
                                     @NotNull EventDispatcher<BuildServerListener> eventDispatcher) {
        long delay = TeamCityProperties.getLong(KUBE_POD_MONITORING_PERIOD, 60);
        executorServices.getNormalExecutorService().scheduleWithFixedDelay(this::populateInstances, delay, delay, TimeUnit.SECONDS);
        // Custom resources (VMs) have no universal status contract; the most reliable readiness
        // signal is the baked-in build agent actually registering on the server.
        eventDispatcher.addListener(new BuildServerAdapter() {
            @Override
            public void agentRegistered(@NotNull SBuildAgent agent, long currentlyRunningBuildId) {
                try {
                    markInstanceRunning(agent);
                } catch (Exception ex) {
                    LOG.warnAndDebugDetails("Failed to update cloud instance status for registered agent " + agent.getName(), ex);
                }
            }
        });
    }

    private void markInstanceRunning(@NotNull SBuildAgent agent) {
        for (KubeCloudClient client : myRegisteredClients) {
            final CloudInstance instance = client.findInstanceByAgent(agent);
            if (instance instanceof KubeCloudInstance) {
                final KubeCloudInstance kubeInstance = (KubeCloudInstance)instance;
                final InstanceStatus status = kubeInstance.getStatus();
                if (status == InstanceStatus.SCHEDULED_TO_START || status == InstanceStatus.STARTING) {
                    LOG.info(String.format("Agent '%s' registered - marking cloud instance '%s' as running", agent.getName(), kubeInstance.getName()));
                    kubeInstance.setStatus(InstanceStatus.RUNNING);
                }
                return;
            }
        }
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
            LOG.debug("Populate instances task finished in " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - populateInstancesStartTime) + " seconds");
        } catch (Exception ex) {
            LOG.warnAndDebugDetails("An error occurred while populating kube instances", ex);
        }
    }
}