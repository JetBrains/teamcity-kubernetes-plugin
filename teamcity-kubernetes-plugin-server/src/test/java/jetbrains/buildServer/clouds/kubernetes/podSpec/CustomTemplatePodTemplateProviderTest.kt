package jetbrains.buildServer.clouds.kubernetes.podSpec

import jetbrains.buildServer.BaseTestCase
import jetbrains.buildServer.clouds.CloudInstanceUserData
import jetbrains.buildServer.clouds.kubernetes.KubePodNameGenerator
import jetbrains.buildServer.serverSide.ServerSettings
import jetbrains.buildServer.serverSide.impl.ServerSettingsImpl
import jetbrains.buildServer.util.TestFor
import org.assertj.core.api.BDDAssertions.then
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

@Test
class CustomTemplatePodTemplateProviderTest : BaseTestCase() {
    
    private lateinit var myServerSettings : ServerSettings
    
    @BeforeMethod
    override fun setUp() {
        super.setUp()
        myServerSettings = object : ServerSettingsImpl() {
            override fun getServerUUID(): String? {
                return "SERVER-UUID"
            }
        }

    }

    @TestFor(issues = ["TW-63014"])
    fun no_labels(){
        val noLabelsYaml = "apiVersion: v1\n" +
                "kind: Pod\n" +
                "metadata:\n" +
                "  name: teamcity-agent\n" +
                "spec:\n" +
                "  containers:\n" +
                "  - name: teamcity-agent\n" +
                "    image: jetbrains/teamcity-agent"
        val provider = CustomTemplatePodTemplateProvider(myServerSettings) { "${it.id}-123" }
        val userData = CloudInstanceUserData("", "", "http://127.0.0.1:9999", null, "kube-321",
                "Test Profile", emptyMap())
        val kubeTemplSpec = provider.getPodTemplateInternal(userData, "kube-img", "namespacccess", "instance-name", noLabelsYaml)
        then(kubeTemplSpec.metadata.labels).containsOnlyKeys("teamcity-cloud-profile", "teamcity-cloud-image", "teamcity-agent", "teamcity-server-uuid")
    }
}