/*
 * Copyright 2000-2021 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.clouds.kubernetes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import com.intellij.openapi.diagnostic.Logger;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClientException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import jetbrains.buildServer.clouds.CloudErrorInfo;
import jetbrains.buildServer.clouds.CloudInstance;
import jetbrains.buildServer.clouds.kubernetes.connector.ImagePullPolicy;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnector;
import jetbrains.buildServer.clouds.kubernetes.podSpec.DeploymentBuildAgentPodTemplateProvider;
import jetbrains.buildServer.clouds.kubernetes.podSpec.SimpleRunContainerProvider;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.ExceptionUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 07.06.17.
 */
public class KubeCloudImageImpl implements KubeCloudImage {
    private static final Logger LOG = Logger.getInstance(KubeCloudImageImpl.class.getName());
    private final KubeApiConnector myApiConnector;
    private final KubeCloudImageData myImageData;
    @Nullable private final String myPodTemplate;
    @Nullable private final String myPVCTemplate;

    private final ConcurrentMap<String, KubeCloudInstance> myIdToInstanceMap = new ConcurrentHashMap<>();
    private CloudErrorInfo myCurrentError;

    KubeCloudImageImpl(@NotNull final KubeCloudImageData kubeCloudImageData,
                       @NotNull final KubeApiConnector apiConnector) {
        myImageData = kubeCloudImageData;
        final String templateContent = myImageData.getCustomPodTemplateContent();
        if (StringUtil.isEmpty(templateContent)){
            myPodTemplate = null;
            myPVCTemplate = null;
        } else if (templateContent.contains("\n---\n")){
            final String processedContent;
            final String placeholderReplacement = "__INSTANCE__ID__";
            if (templateContent.contains("%instance.id%")){
                processedContent = templateContent.replaceAll("%instance.id%", placeholderReplacement);
            } else {
                processedContent = templateContent;
            }
            YAMLFactory factory = new YAMLFactory();
            ObjectMapper mapper = new ObjectMapper();
            final AtomicReference<String> podTemplateRef = new AtomicReference<>();
            final AtomicReference<String> pvcTemplateRef = new AtomicReference<>();
            try {
                YAMLParser parser = factory.createParser(processedContent);
                List<ObjectNode> list = mapper.readValues(parser, ObjectNode.class).readAll();
                list.forEach(node->{
                    if (node.get("kind") != null && "Pod".equals(node.get("kind").textValue())){
                        if (podTemplateRef.get() != null){
                           throw new RuntimeException("More than one Pod template is specified for image " + myImageData.getAgentNamePrefix());
                        }
                        podTemplateRef.set(node.toString().replaceAll(placeholderReplacement, "%instance.id%"));
                    } else if (node.get("kind") != null && "PersistentVolumeClaim".equals(node.get("kind").textValue())){
                        if (pvcTemplateRef.get() != null){
                            throw new RuntimeException("More than one PVC template is specified for image " + myImageData.getAgentNamePrefix());
                        }
                        pvcTemplateRef.set(node.toString().replaceAll(placeholderReplacement, "%instance.id%"));
                    } else{
                        throw new RuntimeException("Unknown yaml template is specified for image " + myImageData.getAgentNamePrefix());
                    }
                });
            } catch (IOException e) {
                ExceptionUtil.rethrowAsRuntimeException(e);
            }
            myPodTemplate = podTemplateRef.get();
            myPVCTemplate = pvcTemplateRef.get();
        } else {
            myPodTemplate = templateContent;
            myPVCTemplate = null;
        }
        myApiConnector = apiConnector;
    }

    @NotNull
    @Override
    public String getPodSpecMode() {
        return myImageData.getPodSpecMode();
    }

    @Nullable
    @Override
    public String getPodTemplate() {
        return myPodTemplate;
    }

    @Nullable
    @Override
    public String getPVCTemplate() {
        return myPVCTemplate;
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


    @Override
    public void setErrorInfo(@Nullable final CloudErrorInfo errorInfo) {
        myCurrentError = errorInfo;
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
        return getId();
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
    public void addStartedInstance(final KubeCloudInstance instance) {
        // don't do anything if present - it's already there
        myIdToInstanceMap.putIfAbsent(instance.getInstanceId(), instance);
    }

    //TODO: synchronize access to myIdToInstanceMap
    //TODO: filter pods more carefully using all setted labels
    public void populateInstances(){
        try{
            final Collection<Pod> pods = myApiConnector.listPods(CollectionsUtil.asMap(
              KubeTeamCityLabels.TEAMCITY_CLOUD_IMAGE, myImageData.getId(),
              KubeTeamCityLabels.TEAMCITY_CLOUD_PROFILE, myImageData.getProfileId()
                                                                 ));
            final Set<String> keys = new HashSet<>(myIdToInstanceMap.keySet());
            final List<Pod> newPods = new ArrayList<>();
            for (Pod pod : pods){
                if (pod.getMetadata() == null){
                    LOG.debug("Found pod without metadata...");
                    continue;
                }
                final String podName = pod.getMetadata().getName();
                if (keys.remove(podName)){
                    LOG.debug(String.format("Found known pod '%s'", podName));
                    final KubeCloudInstance instance = myIdToInstanceMap.get(podName);
                    if (instance == null){
                        LOG.warn(String.format("Instance '%s' was removed?!", podName));
                        continue;
                    }
                    instance.updateState(pod);
                } else {
                    LOG.debug(String.format("Found new pod '%s'", podName));
                    newPods.add(pod);
                }
            }
            if (keys.size() > 0) {
                LOG.info(String.format("The following %d %s %s deleted: %s",
                                       keys.size(), StringUtil.pluralize( "pod",keys.size()),
                                       keys.size() == 1? "was" : "were",
                                       String.join(", ", keys))
                );
                keys.forEach(myIdToInstanceMap::remove);
            }
            if (newPods.size() > 0){
                final List<String> podNames = newPods.stream().map(pod -> pod.getMetadata().getName()).collect(Collectors.toList());
                LOG.info(String.format(
                  "Found %d new %s: %s",
                  newPods.size(),
                  StringUtil.pluralize("pod", newPods.size()),
                  String.join(", ", podNames)
                ));
                newPods.forEach(pod->{
                    final String podName = pod.getMetadata().getName();
                    myIdToInstanceMap.putIfAbsent(podName, new KubeCloudInstanceImpl(this, pod));
                });
            }
            myCurrentError = null;
        } catch (KubernetesClientException ex){
            myCurrentError = new CloudErrorInfo("Failed populate instances", ex.getMessage(), ex);
            throw ex;
        }
    }
}
