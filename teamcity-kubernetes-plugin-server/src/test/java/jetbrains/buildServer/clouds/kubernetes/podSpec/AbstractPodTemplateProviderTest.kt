package jetbrains.buildServer.clouds.kubernetes.podSpec

import io.fabric8.kubernetes.api.model.EnvVar
import io.fabric8.kubernetes.api.model.EnvVarSource
import io.fabric8.kubernetes.api.model.ObjectFieldSelector
import io.fabric8.kubernetes.api.model.Pod
import jetbrains.buildServer.BaseTestCase
import jetbrains.buildServer.clouds.CloudInstanceUserData
import jetbrains.buildServer.clouds.kubernetes.KubeCloudImage
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnector
import jetbrains.buildServer.util.TestFor
import org.assertj.core.api.BDDAssertions.then
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

/**
@author Sergey.Pak
Date: 04.08.21
 */
@Test
class AbstractPodTemplateProviderTest : BaseTestCase() {

    private lateinit var provider: AbstractPodTemplateProvider

    @BeforeMethod
    override fun setUp() {
        super.setUp()
        provider = object: AbstractPodTemplateProvider(){
            override fun getId(): String {
                TODO("Not yet implemented")
            }

            override fun getDisplayName(): String {
                TODO("Not yet implemented")
            }

            override fun getDescription(): String? {
                TODO("Not yet implemented")
            }

            override fun getPodTemplate(kubeInstanceName: String, cloudInstanceUserData: CloudInstanceUserData, kubeCloudImage: KubeCloudImage, apiConnector: KubeApiConnector): Pod {
                TODO("Not yet implemented")
            }
        }
    }

    @TestFor(issues = ["TW-72556"])
    fun patch_env_should_respect_valueFrom(){
        val userData = CloudInstanceUserData("", "", "http://127.0.0.1:9999", null, "kube-321",
                "Test Profile", emptyMap())

        val envVars: MutableList<EnvVar> = arrayListOf()
        val fieldPathVarName = "SAMPLE_FIELDPATH"
        envVars.add(EnvVar(fieldPathVarName, null, EnvVarSource(null, ObjectFieldSelector("v1", "metadata.namespace"), null, null)))

        val patchedEnvVars = provider.getPatchedEnvVars("inst", "serverUUID", "imgId", userData, envVars)
        val patchedRefVar = patchedEnvVars.find { it.name == fieldPathVarName }
        then(patchedRefVar).isNotNull()

        then(patchedRefVar!!.valueFrom).isNotNull()
        then(patchedRefVar.valueFrom.fieldRef.fieldPath).isEqualTo("metadata.namespace")

    }
}