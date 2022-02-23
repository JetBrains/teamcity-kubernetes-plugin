/*
 * Copyright 2000-2022 JetBrains s.r.o.
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

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClientException;
import java.util.concurrent.*;
import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.clouds.CloudClientParameters;
import jetbrains.buildServer.clouds.CloudImage;
import jetbrains.buildServer.clouds.CloudImageParameters;
import jetbrains.buildServer.clouds.CloudInstanceUserData;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnector;
import jetbrains.buildServer.clouds.kubernetes.podSpec.BuildAgentPodTemplateProvider;
import jetbrains.buildServer.clouds.kubernetes.podSpec.BuildAgentPodTemplateProviders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.*;

import static jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants.PROFILE_INSTANCE_LIMIT;
import static org.assertj.core.api.BDDAssertions.then;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 29.06.17.
 */
@Test
public class KubeCloudClientTest extends BaseTestCase {
    private Mockery m;
    private KubeApiConnector myApi;
    private BuildAgentPodTemplateProviders myPodTemplateProviders;
    private Map<String, BuildAgentPodTemplateProvider> myPodTemplateProvidersMap;
    private ExecutorService myExecutorService;

    private KubeBackgroundUpdater myUpdater = new KubeBackgroundUpdater() {
        @Override
        public void registerClient(@NotNull KubeCloudClient client) {

        }

        @Override
        public void unregisterClient(@NotNull KubeCloudClient client) {

        }
    };

    @BeforeMethod
    public void setUp() throws Exception {
        super.setUp();
        m = new Mockery();
        myApi = m.mock(KubeApiConnector.class);
        myExecutorService = new InProcessExecutor();
        myPodTemplateProvidersMap = new HashMap<>();
        myPodTemplateProviders = new BuildAgentPodTemplateProviders() {
            @NotNull
            @Override
            public Collection<BuildAgentPodTemplateProvider> getAll() {
                return myPodTemplateProviders.getAll();
            }

            @Nullable
            @Override
            public BuildAgentPodTemplateProvider find(@Nullable String id) {
                return myPodTemplateProvidersMap.get(id);
            }
        };
    }

    @AfterMethod
    public void tearDown() throws Exception {
        m.assertIsSatisfied();
        super.tearDown();
    }

    public void testIsInitialized() throws Exception {
        assertTrue(createClient(Collections.emptyList()).isInitialized());
    }

    public void testCanStartNewInstance_UnknownImage() throws Exception {
        KubeCloudClient cloudClient = createClient(Collections.emptyList());
        CloudImage image = m.mock(KubeCloudImage.class);
        m.checking(new Expectations(){{
            allowing(image).getId(); will(returnValue("image-1-id"));
        }});
        assertFalse(cloudClient.canStartNewInstance(image));
    }

    public void testCanStartNewInstance() throws Exception {
        KubeCloudImage image = m.mock(KubeCloudImage.class);
        m.checking(new Expectations(){{
            allowing(image).getName(); will(returnValue("image-1-name"));
            allowing(image).getId(); will(returnValue("image-1-id"));
            allowing(image).getRunningInstanceCount(); will(returnValue(0));
            allowing(image).getInstanceLimit(); will(returnValue(0));
        }});
        List<KubeCloudImage> images = Collections.singletonList(image);
        KubeCloudClient cloudClient = createClient(images);
        assertFalse(cloudClient.canStartNewInstance(image));
    }

    @Test
    public void testCanStartNewInstance2() throws Exception {
        KubeCloudImage image = m.mock(KubeCloudImage.class);
        m.checking(new Expectations(){{
            allowing(image).getName(); will(returnValue("image-1-name"));
            allowing(image).getId(); will(returnValue("image-1-id"));
            allowing(image).getRunningInstanceCount(); will(returnValue(1));
            allowing(image).getInstanceLimit(); will(returnValue(5));
        }});
        List<KubeCloudImage> images = Collections.singletonList(image);
        KubeCloudClient cloudClient = createClient(images);
        assertTrue(cloudClient.canStartNewInstance(image));
    }

    public void testCanStartNewInstance_ProfileLimit() throws Exception {
        KubeCloudImage image = m.mock(KubeCloudImage.class);
        m.checking(new Expectations(){{
            allowing(image).getId(); will(returnValue("image-1-id"));
            allowing(image).getRunningInstanceCount(); will(returnValue(1));
            allowing(image).getInstanceLimit(); will(returnValue(2));
        }});
        List<KubeCloudImage> images = Collections.singletonList(image);
        CloudClientParameters cloudClientParameters = new MockCloudClientParameters(Collections.singletonMap(PROFILE_INSTANCE_LIMIT, "1"));
        KubeCloudClient cloudClient = createClient(images, cloudClientParameters);
        assertFalse(cloudClient.canStartNewInstance(image));
    }

    public void testCanStartNewInstance_ProfileLimit2() throws Exception {
        KubeCloudImage image = m.mock(KubeCloudImage.class);
        m.checking(new Expectations(){{
            allowing(image).getId(); will(returnValue("image-1-id"));
            allowing(image).getRunningInstanceCount(); will(returnValue(3));
            allowing(image).getInstanceLimit(); will(returnValue(5));
        }});
        List<KubeCloudImage> images = Collections.singletonList(image);
        CloudClientParameters cloudClientParameters = new MockCloudClientParameters(Collections.singletonMap(PROFILE_INSTANCE_LIMIT, "5"));
        KubeCloudClient cloudClient = createClient(images, cloudClientParameters);
        assertTrue(cloudClient.canStartNewInstance(image));
    }

    public void testCanStartNewInstance_ImageLimit() throws Exception {
        KubeCloudImage image = m.mock(KubeCloudImage.class);
        m.checking(new Expectations(){{
            allowing(image).getName(); will(returnValue("image-1-name"));
            allowing(image).getId(); will(returnValue("image-1-id"));
            allowing(image).getRunningInstanceCount(); will(returnValue(1));
            allowing(image).getInstanceLimit(); will(returnValue(1));
        }});
        List<KubeCloudImage> images = Collections.singletonList(image);
        KubeCloudClient cloudClient = createClient(images);
        assertFalse(cloudClient.canStartNewInstance(image));
    }

    public void testDuplicateImageName() throws Exception {
        KubeCloudImage image1 = m.mock(KubeCloudImage.class, "1");
        KubeCloudImage image2 = m.mock(KubeCloudImage.class, "2");
        m.checking(new Expectations(){{
            allowing(image1).getId(); will(returnValue("image-1-id"));
            allowing(image1).getName(); will(returnValue("image"));
            allowing(image1).getRunningInstanceCount(); will(returnValue(0));
            allowing(image2).getId(); will(returnValue("image-2-id"));
            allowing(image2).getName(); will(returnValue("image"));
            allowing(image2).getRunningInstanceCount(); will(returnValue(0));
        }});
        createClient(Arrays.asList(image1, image2));
    }

    public void start_new_instance_failure_pvc() throws InterruptedException {
        final String podSpecMode = "DUMMY_SPEC_MODE";
        final CloudInstanceUserData userData = createInstanceTag();
        final BuildAgentPodTemplateProvider provider = m.mock(BuildAgentPodTemplateProvider.class);
        KubeCloudImage image = m.mock(KubeCloudImage.class);
        KubeCloudClientParametersImpl parameters = KubeCloudClientParametersImpl.create(new MockCloudClientParameters(Collections.emptyMap()));
        KubeApiConnector apiConnector = m.mock(KubeApiConnector.class);
        Pod podTemplate = new Pod();
        ObjectMeta podMetadata = new ObjectMeta();
        podMetadata.setName("image-1-id-123");
        podTemplate.setMetadata(podMetadata);
        podMetadata.setLabels(new HashMap<>());

        PersistentVolumeClaim pvcTemplate = new PersistentVolumeClaim();
        PersistentVolumeClaim newPVC = new PersistentVolumeClaim();
        {
            ObjectMeta pvcMetadata = new ObjectMeta();
            pvcMetadata.setName("image-1-id-123");
            pvcTemplate.setMetadata(pvcMetadata);
        }
        {
            ObjectMeta pvcMetadata = new ObjectMeta();
            pvcMetadata.setName("image-1-id-123");
            newPVC.setMetadata(pvcMetadata);
        }

        KubernetesClientException podCreationException = new KubernetesClientException("Some error on pod creation");

        myPodTemplateProvidersMap.put(podSpecMode, provider);

        m.checking(new Expectations(){{
            allowing(image).getName(); will(returnValue("image-1-name"));
            allowing(image).getId(); will(returnValue("image-1-id"));
            allowing(image).getPodSpecMode(); will(returnValue(podSpecMode));
            allowing(image).addStartedInstance(with(any(KubeCloudInstance.class)));
            allowing(apiConnector).getNamespace(); will(returnValue("leer"));
            oneOf(provider).getPodTemplate("image-1-id-123", userData, image, apiConnector); will(returnValue(podTemplate));
            oneOf(provider).getPVC("image-1-id-123", image); will(returnValue(pvcTemplate));
            oneOf(myApi).createPVC(pvcTemplate); will(returnValue(newPVC));
            oneOf(myApi).createPod(podTemplate); will(throwException(podCreationException));
            oneOf(myApi).deletePVC("image-1-id-123"); // should delete PVC if can't create pod
        }});
        List<KubeCloudImage> images = Collections.singletonList(image);
        KubeCloudClient cloudClient = createClient("defaultServerUuid", "defaultProfileId", images, parameters);
        try {
            cloudClient.startNewInstance(image, userData);
            fail("An exception " + podCreationException + " should have been rethrown");
        } catch (Exception ex) {
            then(ex).isEqualTo(podCreationException);
        }
        m.assertIsSatisfied();
    }

    @NotNull
    private KubeCloudClient createClient(List<KubeCloudImage> images) {
        return createClient("defaultServerUuid", "defaultProfileId", images, new MockCloudClientParameters(Collections.emptyMap()));
    }

    @NotNull
    private KubeCloudClient createClient(List<KubeCloudImage> images, CloudClientParameters cloudClientParameters) {
        return createClient("defaultServerUuid", "defaultProfileId", images, cloudClientParameters);
    }

    @NotNull
    private KubeCloudClient createClient(String serverUuid, String profileId, List<KubeCloudImage> images, CloudClientParameters cloudClientParameters) {
        return createClient(serverUuid, profileId, images, new KubeCloudClientParametersImpl(cloudClientParameters));
    }
    @NotNull
    private KubeCloudClient createClient(String serverUuid, String profileId, List<KubeCloudImage> images, KubeCloudClientParameters parameters) {
        return new KubeCloudClient(myApi, serverUuid, profileId, images, parameters, myUpdater,
                                   myPodTemplateProviders, myExecutorService, image -> String.format("%s-123", image.getId()));
    }

    private CloudInstanceUserData createInstanceTag() {
        return new CloudInstanceUserData("agent name", "auth token", "server address", null, "profile id", "profile description", Collections.emptyMap());
    }

    private static class InProcessExecutor extends AbstractExecutorService {
        @Override
        public void shutdown() {
        }

        @Override
        @NotNull
        public List<Runnable> shutdownNow() {
            return null;
        }

        @Override
        public boolean isShutdown() {
            return false;
        }

        @Override
        public boolean isTerminated() {
            return false;
        }

        @Override
        public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
            return true;
        }

        @Override
        public void execute(@NotNull final Runnable command) {
            command.run();
        }

        @NotNull
        @Override
        public Future<?> submit(@NotNull Runnable task) {
            execute(task);
            return new CompletableFuture<>();
        }
    }


    private class MockCloudClientParameters extends CloudClientParameters {
        private final Map<String, String> myParameters;

        public MockCloudClientParameters(Map<String, String> parameters) {
            myParameters = parameters;
        }

        @Nullable
        @Override
        public String getParameter(@NotNull String s) {
            return myParameters.get(s);
        }

        @NotNull
        @Override
        public Collection<String> listParameterNames() {
            return myParameters.keySet();
        }

        @NotNull
        @Override
        public Collection<CloudImageParameters> getCloudImages() {
            return null;
        }

        @NotNull
        @Override
        public Map<String, String> getParameters() {
            return Collections.unmodifiableMap(myParameters);
        }

        @NotNull
        @Override
        public String getProfileId() {
            return null;
        }

        @NotNull
        @Override
        public String getProfileDescription() {
            return null;
        }
    }
}