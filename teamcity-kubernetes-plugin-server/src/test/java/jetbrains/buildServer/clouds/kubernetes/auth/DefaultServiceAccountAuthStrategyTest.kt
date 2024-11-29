package jetbrains.buildServer.clouds.kubernetes.auth

import jetbrains.buildServer.clouds.CloudConstants
import jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants
import jetbrains.buildServer.serverSide.impl.BaseServerTestCase
import jetbrains.buildServer.serverSide.oauth.OAuthConstants
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.util.*

class DefaultServiceAccountAuthStrategyTest : BaseServerTestCase() {
    private lateinit var defaultServiceAccountAuthStrategy: DefaultServiceAccountAuthStrategy

    @BeforeMethod
    override fun setUp() {
        super.setUp()
        defaultServiceAccountAuthStrategy = DefaultServiceAccountAuthStrategy(myProjectManager)
    }

    @Test
    fun testIsAvailable(){
        Assert.assertFalse(defaultServiceAccountAuthStrategy.isAvailable(myProject.projectId))
    }

    @Test
    fun testIsAvailableWhenEnabled(){
        setInternalProperty(DefaultServiceAccountAuthStrategy.TEAMCITY_KUBERNETES_LOCAL_SERVICE_ACCOUNT_ENABLE, "true")
        Assert.assertTrue(defaultServiceAccountAuthStrategy.isAvailable(myProject.projectId))
    }

    @Test
    fun testIsAvailableWhenCloudProfileWithItIsAvailable(){
        myProject.addFeature(
            CloudConstants.CLOUD_PROFILE_FEATURE_TYPE,
            Collections.singletonMap(KubeParametersConstants.AUTH_STRATEGY, defaultServiceAccountAuthStrategy.id)
        );
        Assert.assertTrue(defaultServiceAccountAuthStrategy.isAvailable(myProject.projectId))
    }

    @Test
    fun testIsAvailableWhenConnectionWithItIsAvailable(){
        myProject.addFeature(
            OAuthConstants.FEATURE_TYPE,
            Collections.singletonMap(KubeParametersConstants.AUTH_STRATEGY, defaultServiceAccountAuthStrategy.id)
        );
        Assert.assertTrue(defaultServiceAccountAuthStrategy.isAvailable(myProject.projectId))
    }
}