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
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodStatusBuilder;
import java.util.*;
import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnector;
import jetbrains.buildServer.serverSide.AgentDescription;
import jetbrains.buildServer.serverSide.RunType;
import jetbrains.buildServer.serverSide.agentTypes.AgentDescriptionBase;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.text.SimpleDateFormat;

import static jetbrains.buildServer.asserts.CommonAsserts.then;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 29.06.17.
 */
@Test
public class KubeCloudInstanceImplTest extends BaseTestCase {
    private Mockery m;
    private KubeApiConnector myApi;
    private KubeCloudImage myImage;

    @BeforeMethod
    public void setUp() throws Exception {
        super.setUp();
        m = new Mockery();
        myApi = m.mock(KubeApiConnector.class);
        myImage = m.mock(KubeCloudImage.class);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        m.assertIsSatisfied();
        super.tearDown();
    }

    public void testGetStartedTime() {
        ObjectMeta metadata = new ObjectMeta();
        metadata.setName("foo");
        Pod pod = new Pod("1.0", "kind", metadata, new PodSpec(), new PodStatusBuilder().withStartTime("2017-06-12T22:59Z").build());
        m.checking(new Expectations(){{
            allowing(myApi).getPodStatus(with("foo")); will(returnValue(pod.getStatus()));
        }});
        KubeCloudInstanceImpl instance = new KubeCloudInstanceImpl(myImage, pod);
        Date startedTime = instance.getStartedTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        assertEquals("2017-06-12T22:59Z", dateFormat.format(startedTime));
    }

    public void match_instance_name(){
        Pod pod = new Pod();
        ObjectMeta metadata = new ObjectMeta();
        metadata.setName("my-instance-2");
        pod.setMetadata(metadata);
        KubeCloudInstance instance = new KubeCloudInstanceImpl(myImage, pod);
        final Map<String, String> buildParams = new HashMap<>();
        buildParams.put("env." + KubeContainerEnvironment.INSTANCE_NAME, "my-instance-1");
        final AgentDescription description = new AgentDescriptionBase(
          createMap(), buildParams, false ) {
            public List<RunType> getAvailableRunTypes() {return null;}
            public Set<String> getAvailableRunTypeIds() {return null;}
            public List<String> getAvailableVcsPlugins() {return null;}
            public String getOperatingSystemName() {return null;}
            public int getCpuBenchmarkIndex() {return 0;}
        };
        buildParams.put("env." + KubeContainerEnvironment.INSTANCE_NAME, "my-instance-2");
        buildParams.put(KubeContainerEnvironment.INSTANCE_NAME, "my-instance-1");
        then(instance.containsAgent(description)).isFalse();

    }
}