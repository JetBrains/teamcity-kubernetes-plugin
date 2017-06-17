package ekoshkin.teamcity.clouds.kubernetes;

import ekoshkin.teamcity.clouds.kubernetes.connector.ImagePullPolicy;
import ekoshkin.teamcity.clouds.kubernetes.connector.KubeApiConnector;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClientException;
import jetbrains.buildServer.clouds.CloudErrorInfo;
import jetbrains.buildServer.clouds.CloudInstance;
import jetbrains.buildServer.util.CollectionsUtil;
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
    private Map<String, KubeCloudInstance> myIdToInstanceMap = new ConcurrentHashMap<String, KubeCloudInstance>();
    private CloudErrorInfo myCurrentError;

    public KubeCloudImageImpl(@NotNull final KubeCloudImageData kubeCloudImageData,
                              @NotNull final KubeApiConnector apiConnector) {
        myImageData = kubeCloudImageData;
        myApiConnector = apiConnector;
    }

    @NotNull
    @Override
    public String getPodSpecMode() {
        return myImageData.getPodSpecMode();
    }

    @Nullable
    @Override
    public String getCustomPodTemplateContent() {
        return myImageData.getCustomPodTemplateContent();
    }

    @NotNull
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
        return myImageData.getName();
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

    @Override
    public void addInstance(@NotNull KubeCloudInstance instance){
        myIdToInstanceMap.put(instance.getInstanceId(), instance);
    }

    @Override
    public boolean deleteInstance(@NotNull KubeCloudInstance instance){
        return myIdToInstanceMap.remove(instance.getInstanceId()) != null;
    }

    void populateInstances(){
        try{
            for (Pod pod : myApiConnector.listPods(CollectionsUtil.asMap(KubeTeamCityLabels.TEAMCITY_CLOUD_IMAGE, myImageData.getId()))){
                KubeCloudInstanceImpl cloudInstance = new KubeCloudInstanceImpl(this, pod, myApiConnector);
                myIdToInstanceMap.put(cloudInstance.getInstanceId(), cloudInstance);
            }
            myCurrentError = null;
        } catch (KubernetesClientException ex){
            myCurrentError = new CloudErrorInfo("Failed populate instances", ex.getMessage(), ex);
        }
    }
}
