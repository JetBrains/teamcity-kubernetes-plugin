package jetbrains.buildServer.clouds.kubernetes;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClientException;
import jetbrains.buildServer.clouds.CloudErrorInfo;
import jetbrains.buildServer.clouds.CloudInstance;
import jetbrains.buildServer.clouds.CloudInstanceUserData;
import jetbrains.buildServer.clouds.kubernetes.connector.ImagePullPolicy;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnector;
import jetbrains.buildServer.clouds.kubernetes.podSpec.BuildAgentPodTemplateProvider;
import jetbrains.buildServer.clouds.kubernetes.podSpec.BuildAgentPodTemplateProviders;
import jetbrains.buildServer.clouds.kubernetes.podSpec.DeploymentBuildAgentPodTemplateProvider;
import jetbrains.buildServer.clouds.kubernetes.podSpec.SimpleRunContainerProvider;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 07.06.17.
 */
public class KubeCloudImageImpl implements KubeCloudImage {
    private final KubeApiConnector myApiConnector;
    private final KubeCloudImageData myImageData;
    private final KubeDataCache myCache;
    private final BuildAgentPodTemplateProviders myPodTemplateProviders;

    private Map<String, KubeCloudInstance> myIdToInstanceMap = new ConcurrentHashMap<>();
    private CloudErrorInfo myCurrentError;

    KubeCloudImageImpl(@NotNull final KubeCloudImageData kubeCloudImageData,
                       @NotNull final KubeApiConnector apiConnector,
                       @NotNull final KubeDataCache cache,
                       @NotNull final BuildAgentPodTemplateProviders podTemplateProviders) {
        myImageData = kubeCloudImageData;
        myApiConnector = apiConnector;
        myCache = cache;
        myPodTemplateProviders = podTemplateProviders;
    }

    @NotNull
    @Override
    public String getPodSpecMode() {
        return myImageData.getPodSpecMode();
    }

    @Nullable
    @Override
    public String getCustomPodTemplateSpec() {
        return myImageData.getCustomPodTemplateContent();
    }

    @Nullable
    @Override
    public String getSourceDeploymentName() {
        return myImageData.getDeploymentName();
    }

    @Override
    public int getRunningInstanceCount() {
        return (int) myIdToInstanceMap.values().stream().filter(kubeCloudInstance -> kubeCloudInstance.getStatus().isStartingOrStarted()).count();
    }

    @Override
    public int getInstanceLimit() {
        return myImageData.getInstanceLimit();
    }

    @NotNull
    @Override
    public String getAgentName(@NotNull String instanceName) {
        final String agentNamePrefix = myImageData.getAgentNamePrefix();
        return StringUtil.isEmpty(agentNamePrefix) ? instanceName : agentNamePrefix + instanceName;
    }

    @Nullable
    @Override
    public String getAgentNamePrefix() {
        return myImageData.getAgentNamePrefix();
    }

    @NotNull
    @Override
    public String getAgentNamePrefix() {
        return myImageData.getAgentNamePrefix();
    }

    @NotNull
    @Override
    public CloudInstance startNewInstance(@NotNull CloudInstanceUserData instanceUserData, @NotNull KubeCloudClientParametersImpl clientParams) {
        final KubeCloudInstance newInstance;
        BuildAgentPodTemplateProvider podTemplateProvider = myPodTemplateProviders.get(getPodSpecMode());
        try {
            final Pod podTemplate = podTemplateProvider.getPodTemplate(instanceUserData, this, clientParams);
            final Pod newPod = myApiConnector.createPod(podTemplate);
            myCurrentError = null;
            newInstance = new CachingKubeCloudInstance(new KubeCloudInstanceImpl(this, newPod, myApiConnector), myCache);
        } catch (KubeCloudException | KubernetesClientException ex){
            myCurrentError = new CloudErrorInfo("Failed to start pod", ex.getMessage(), ex);
            throw ex;
        }
        populateInstances();
        return newInstance;
    }

    @Nullable
    @Override
    public String getDockerImage() {
        return myImageData.getDockerImage();
    }

    @Nullable
    @Override
    public ImagePullPolicy getImagePullPolicy() {
        return myImageData.getImagePullPolicy();
    }

    @Nullable
    @Override
    public String getDockerArguments() {
        return myImageData.getDockerArguments();
    }

    @Nullable
    @Override
    public String getDockerCommand() {
        return myImageData.getDockerCommand();
    }

    @NotNull
    @Override
    public String getId() {
        return myImageData.getId();
    }

    @NotNull
    @Override
    public String getName() {
        switch (getPodSpecMode()){
            case SimpleRunContainerProvider.ID:
                return "Docker Image: " + getDockerImage();
            case DeploymentBuildAgentPodTemplateProvider.ID:
                return "Deployment: " + getSourceDeploymentName();
            default:
                return "UNKNOWN";
        }
    }

    @NotNull
    @Override
    public Collection<? extends CloudInstance> getInstances() {
        return myIdToInstanceMap.values();
    }

    @Nullable
    @Override
    public CloudInstance findInstanceById(@NotNull String id) {
        return myIdToInstanceMap.get(id);
    }

    @Nullable
    @Override
    public Integer getAgentPoolId() {
        return myImageData.getAgentPoolId();
    }

    @Nullable
    @Override
    public CloudErrorInfo getErrorInfo() {
        return myCurrentError;
    }

    //TODO: syncronize access to myIdToInstanceMap
    //TODO: filter pods more carefully using all setted labels
    public void populateInstances(){
        try{
            final Collection<Pod> pods = myApiConnector.listPods(CollectionsUtil.asMap(
              KubeTeamCityLabels.TEAMCITY_CLOUD_IMAGE, myImageData.getId(),
              KubeTeamCityLabels.TEAMCITY_CLOUD_PROFILE, myImageData.getProfileId()
                                                                 ));
            myIdToInstanceMap.clear();
            for (Pod pod : pods){
                KubeCloudInstance cloudInstance = new CachingKubeCloudInstance(new KubeCloudInstanceImpl(this, pod, myApiConnector), myCache);
                String instanceId = cloudInstance.getInstanceId();
                myIdToInstanceMap.put(instanceId, cloudInstance);
                myCache.cleanInstanceStatus(instanceId);
            }
            myCurrentError = null;
        } catch (KubernetesClientException ex){
            myCurrentError = new CloudErrorInfo("Failed populate instances", ex.getMessage(), ex);
            throw ex;
        }
    }
}
