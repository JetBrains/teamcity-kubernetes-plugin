/*
 * Copyright 2000-2020 JetBrains s.r.o.
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

package jetbrains.buildServer.clouds.kubernetes

import io.fabric8.kubernetes.api.model.ObjectMeta
import io.fabric8.kubernetes.api.model.Pod
import jetbrains.buildServer.BaseTestCase
import jetbrains.buildServer.clouds.CloudImageParameters
import jetbrains.buildServer.clouds.CloudInstanceUserData
import jetbrains.buildServer.clouds.CloudKeys
import jetbrains.buildServer.clouds.kubernetes.connector.FakeKubeApiConnector
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnector
import jetbrains.buildServer.clouds.kubernetes.podSpec.BuildAgentPodTemplateProvider
import jetbrains.buildServer.clouds.kubernetes.podSpec.BuildAgentPodTemplateProviders
import jetbrains.buildServer.clouds.server.impl.profile.CloudImageDataImpl
import jetbrains.buildServer.clouds.server.impl.profile.CloudImageParametersImpl
import jetbrains.buildServer.util.executors.ExecutorsFactory
import org.assertj.core.api.BDDAssertions.then
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.util.concurrent.Executors

@Test
class KubeCloudImageTest : BaseTestCase() {

    private val PROJECT_ID = "project123"
    private lateinit var myApiConnector: KubeApiConnector
    private var myPods: MutableList<Pair<Pod, Map<String, String>>> = arrayListOf()

    @BeforeMethod
    override fun setUp() {
        super.setUp()
        myPods = arrayListOf()
        myApiConnector = object: FakeKubeApiConnector(){
            override fun listPods(labels: MutableMap<String, String>): MutableCollection<Pod> {
                return myPods.filter {
                    var retval = true
                    labels.forEach { k, v -> retval = retval &&(it.second.containsKey(k) && it.second[k] == v) }
                    retval
                }.map { it.first }.toMutableList()
            }
        }
    }

    public fun check_populate_instances(){

        val image1Profile1 = createImage(createMap(
                CloudImageParameters.SOURCE_ID_FIELD, "image1",
                CloudKeys.PROFILE_ID, "kube-1",
                KubeParametersConstants.DOCKER_IMAGE, "jetbrains/teamcity-agent"))

        val image1Profile2 = createImage(createMap(
                CloudImageParameters.SOURCE_ID_FIELD, "image1",
                CloudKeys.PROFILE_ID, "kube-2",
                KubeParametersConstants.DOCKER_IMAGE, "jetbrains/teamcity-agent"))
        val pod = Pod()
        pod.metadata = ObjectMeta()
        pod.metadata.name = "jetbrains-teamcity-agent-1";
        myPods.add(Pair(pod, createMap(
                KubeTeamCityLabels.TEAMCITY_CLOUD_PROFILE, "kube-1",
                KubeTeamCityLabels.TEAMCITY_CLOUD_IMAGE, image1Profile1.id)))
        image1Profile1.populateInstances()
        then(image1Profile1.instances).hasSize(1)
        then(image1Profile1.instances.first().name).isEqualTo("jetbrains-teamcity-agent-1")
        image1Profile2.populateInstances()
        then(image1Profile2.instances).hasSize(0)
    }

    private fun createImage(imageData: Map<String, String> = emptyMap()): KubeCloudImage {
        var cloudImageData = CloudImageDataImpl(imageData)
        val buildAgentPodTemplateProviders = object : BuildAgentPodTemplateProviders {
            override fun getAll(): MutableCollection<BuildAgentPodTemplateProvider> {
                return arrayListOf()
            }

            override fun find(id: String?): BuildAgentPodTemplateProvider? {
                return null;
            }

            override fun get(id: String?): BuildAgentPodTemplateProvider {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }
        return KubeCloudImageImpl(KubeCloudImageData(CloudImageParametersImpl(cloudImageData, PROJECT_ID, "image1")),
                myApiConnector, buildAgentPodTemplateProviders, ExecutorsFactory.newFixedScheduledExecutor("", 1))
    }

    private fun createInstanceTag(): CloudInstanceUserData {
        return CloudInstanceUserData("agent name", "auth token", "server address", null, "profile id", "profile description", emptyMap())
    }
}