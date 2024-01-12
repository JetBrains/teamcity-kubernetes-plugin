
package jetbrains.buildServer.clouds.kubernetes

import io.fabric8.kubernetes.api.model.ObjectMeta
import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.api.model.PodStatus
import jetbrains.buildServer.BaseTestCase
import jetbrains.buildServer.clouds.CloudImageParameters
import jetbrains.buildServer.clouds.CloudInstanceUserData
import jetbrains.buildServer.clouds.CloudKeys
import jetbrains.buildServer.clouds.InstanceStatus
import jetbrains.buildServer.clouds.kubernetes.connector.FakeKubeApiConnector
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnector
import jetbrains.buildServer.clouds.kubernetes.podSpec.BuildAgentPodTemplateProvider
import jetbrains.buildServer.clouds.kubernetes.podSpec.BuildAgentPodTemplateProviders
import jetbrains.buildServer.clouds.server.impl.profile.CloudImageDataImpl
import jetbrains.buildServer.clouds.server.impl.profile.CloudImageParametersImpl
import jetbrains.buildServer.util.TestFor
import org.assertj.core.api.BDDAssertions.then
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

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

        val image1Profile1 = createImage("image1")
        val image1Profile2 = createImage("image2", "kube-2")

        val pod = createPod("jetbrains-teamcity-agent-1")

        myPods.add(Pair(pod, podLabels("kube-1", image1Profile1.id)))
        image1Profile1.populateInstances()
        then(image1Profile1.instances).hasSize(1)
        then(image1Profile1.instances.first().name).isEqualTo("jetbrains-teamcity-agent-1")
        image1Profile2.populateInstances()
        then(image1Profile2.instances).hasSize(0)
    }


    @TestFor(issues=["TW-52354"])
    public fun dont_drop_scheduled_2_start_during_populate(){
        val img = createImage("image1")
        val pod = createPod("jetbrains-teamcity-agent-1")
        img.addStartedInstance(KubeCloudInstanceImpl(img, pod))
        img.populateInstances()
        then(img.instances).hasSize(1)
        then(img.instances.first().name).isEqualTo("jetbrains-teamcity-agent-1")

        myPods.add(Pair(pod, podLabels("kube-1", img.id)))
        pod.status = PodStatus()
        pod.status.phase = "Running"
        img.populateInstances()
        then(img.instances).hasSize(1)
        then(img.instances.first().status).isEqualTo(InstanceStatus.RUNNING)
    }

    public fun drop_non_existing(){
        val img = createImage("image1")
        val pod = createPod("jetbrains-teamcity-agent-1")
        val instance = KubeCloudInstanceImpl(img, pod)
        img.addStartedInstance(instance)
        instance.status = InstanceStatus.RUNNING
        img.populateInstances()
        then(img.instances).isEmpty()
    }

    @TestFor(issues=["TW-80102"])
    public fun handle_deleted_externally(){
        val img = createImage("image1")
        val pod = createPod("jetbrains-teamcity-agent-1")
        myPods.add(Pair(pod, podLabels("kube-1", img.id)))
        pod.status = PodStatus()
        pod.status.phase = "Running"

        img.addStartedInstance(KubeCloudInstanceImpl(img, pod))
        img.populateInstances()
        then(img.instances).hasSize(1)
        then(img.instances.first().status).isEqualTo(InstanceStatus.RUNNING)

        myPods.clear()
        img.populateInstances()
        then(img.instances).hasSize(0)
    }

    private fun KubeCloudImageTest.podLabels(profileId: String, imageId: String) : Map<String, String> =
        createMap(
            KubeTeamCityLabels.TEAMCITY_CLOUD_PROFILE, profileId,
            KubeTeamCityLabels.TEAMCITY_CLOUD_IMAGE, imageId
        )

    private fun createPod(podName: String): Pod {
        val pod = Pod()
        pod.metadata = ObjectMeta()
        pod.metadata.name = podName;
        return pod
    }
    private fun createImage(sourceId: String, profileId: String = "kube-1"): KubeCloudImage {
        var cloudImageData = CloudImageDataImpl(
            createMap(
                CloudImageParameters.SOURCE_ID_FIELD, sourceId,
                CloudKeys.PROFILE_ID, profileId,
                KubeParametersConstants.DOCKER_IMAGE, "jetbrains/teamcity-agent"
            )
        )
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
        return KubeCloudImageImpl(
            KubeCloudImageData(CloudImageParametersImpl(cloudImageData, PROJECT_ID, "image1")),
            myApiConnector
        )
    }

    private fun createInstanceTag(): CloudInstanceUserData {
        return CloudInstanceUserData("agent name", "auth token", "server address", null, "profile id", "profile description", emptyMap())
    }
}