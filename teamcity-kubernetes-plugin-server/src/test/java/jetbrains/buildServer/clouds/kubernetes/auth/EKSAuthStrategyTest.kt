package jetbrains.buildServer.clouds.kubernetes.auth

import jetbrains.buildServer.clouds.CloudConstants
import jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants
import jetbrains.buildServer.serverSide.impl.BaseServerTestCase
import jetbrains.buildServer.serverSide.oauth.OAuthConstants
import jetbrains.buildServer.util.SystemTimeService
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.util.*

class EKSAuthStrategyTest : BaseServerTestCase() {
    private lateinit var strategy: EKSAuthStrategy

    @BeforeMethod
    override fun setUp() {
        super.setUp()
        strategy = EKSAuthStrategy(SystemTimeService(), myProjectManager)
    }

    @Test
    fun testIsLocalEksAvailable(){
        Assert.assertFalse(isLocalEksAvailable())
    }

    @Test
    fun testIsLocalEksAvailableWhenEnabled(){
        setInternalProperty(EKSAuthStrategy.ENABLE_LOCAL_AWS_ACCOUNT, "true")
        Assert.assertTrue(isLocalEksAvailable())
    }

    @Test
    fun testIsLocalEksAvailableWhenCloudProfileWithItIsAvailable(){
        myProject.addFeature(
            CloudConstants.CLOUD_PROFILE_FEATURE_TYPE,
            mapOf(KubeParametersConstants.AUTH_STRATEGY to strategy.id,
                KubeParametersConstants.EKS_USE_INSTANCE_PROFILE to "true")
        );
        Assert.assertTrue(isLocalEksAvailable())
    }

    @Test
    fun testIsLocalEksAvailableWhenConnectionWithItIsAvailable(){
        myProject.addFeature(
            OAuthConstants.FEATURE_TYPE,
            mapOf(KubeParametersConstants.AUTH_STRATEGY to strategy.id,
                KubeParametersConstants.EKS_USE_INSTANCE_PROFILE to "true")
        );
        Assert.assertTrue(isLocalEksAvailable())
    }

    private fun isLocalEksAvailable(): Boolean {
        val map = mutableMapOf<String, Any>()
        strategy.fillAdditionalSettings(map, myProject.projectId, true)

        return map[KubeParametersConstants.EKS_USE_INSTANCE_PROFILE_ENABLED].toString().toBoolean()
    }
}