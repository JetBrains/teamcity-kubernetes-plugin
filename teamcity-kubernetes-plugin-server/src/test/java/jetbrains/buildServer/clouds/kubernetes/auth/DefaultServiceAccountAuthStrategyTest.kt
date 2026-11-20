package jetbrains.buildServer.clouds.kubernetes.auth

import io.fabric8.kubernetes.client.ConfigBuilder
import jetbrains.buildServer.MockTimeService
import jetbrains.buildServer.clouds.CloudConstants
import jetbrains.buildServer.clouds.kubernetes.KubeCloudException
import jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants
import jetbrains.buildServer.clouds.kubernetes.KubeProfilePropertiesProcessor
import jetbrains.buildServer.clouds.kubernetes.ParametersKubeApiConnection
import jetbrains.buildServer.serverSide.impl.BaseServerTestCase
import jetbrains.buildServer.serverSide.oauth.OAuthConstants
import jetbrains.buildServer.util.TestFor
import org.assertj.core.api.BDDAssertions.then
import org.assertj.core.api.BDDAssertions.thenThrownBy
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.util.*

class DefaultServiceAccountAuthStrategyTest : BaseServerTestCase() {
    private lateinit var defaultServiceAccountAuthStrategy: DefaultServiceAccountAuthStrategy

    @BeforeMethod(alwaysRun = true)
    override fun setUp() {
        super.setUp()
        defaultServiceAccountAuthStrategy = DefaultServiceAccountAuthStrategy(myProjectManager, createTempFile(TOKEN))
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

    @Test
    fun testApplyIsRejectedWhenDisabled() {
        thenThrownBy { defaultServiceAccountAuthStrategy.apply(ConfigBuilder(), ParametersKubeApiConnection(profileParameters())) }
            .isInstanceOf(KubeCloudException::class.java)
            .hasMessageContaining(DefaultServiceAccountAuthStrategy.DISABLED_MESSAGE)
            .hasMessageNotContaining(DefaultServiceAccountAuthStrategy.TEAMCITY_KUBERNETES_LOCAL_SERVICE_ACCOUNT_ENABLE)
    }

    @Test
    fun testApplyIsRejectedWhenDisabledEvenIfProjectAlreadyUsesIt() {
        myProject.addFeature(CloudConstants.CLOUD_PROFILE_FEATURE_TYPE, profileParameters())

        thenThrownBy { defaultServiceAccountAuthStrategy.apply(ConfigBuilder(), ParametersKubeApiConnection(profileParameters())) }
            .isInstanceOf(KubeCloudException::class.java)
            .hasMessageContaining(DefaultServiceAccountAuthStrategy.DISABLED_MESSAGE)
            .hasMessageNotContaining(DefaultServiceAccountAuthStrategy.TEAMCITY_KUBERNETES_LOCAL_SERVICE_ACCOUNT_ENABLE)
    }

    @Test
    fun testApplyUsesServiceAccountTokenWhenEnabled() {
        setInternalProperty(DefaultServiceAccountAuthStrategy.TEAMCITY_KUBERNETES_LOCAL_SERVICE_ACCOUNT_ENABLE, "true")

        then(defaultServiceAccountAuthStrategy.apply(ConfigBuilder(), ParametersKubeApiConnection(profileParameters())).oauthToken)
            .isEqualTo(TOKEN)
    }

    @Test
    fun testSavingProfileIsRejectedWhenDisabled() {
        val invalidProperties = profilePropertiesProcessor().process(profileParameters())

        then(invalidProperties.map { it.propertyName }).containsExactly(KubeParametersConstants.AUTH_STRATEGY)
        then(invalidProperties.single().invalidReason)
            .contains(DefaultServiceAccountAuthStrategy.DISABLED_MESSAGE)
            .doesNotContain(DefaultServiceAccountAuthStrategy.TEAMCITY_KUBERNETES_LOCAL_SERVICE_ACCOUNT_ENABLE)
    }

    @Test
    fun testSavingProfileIsAllowedWhenEnabled() {
        setInternalProperty(DefaultServiceAccountAuthStrategy.TEAMCITY_KUBERNETES_LOCAL_SERVICE_ACCOUNT_ENABLE, "true")

        then(profilePropertiesProcessor().process(profileParameters())).isEmpty()
    }

    private fun profilePropertiesProcessor() =
        KubeProfilePropertiesProcessor(KubeAuthStrategyProviderImpl(MockTimeService(), myProjectManager))

    private fun profileParameters() = mutableMapOf(
        KubeParametersConstants.AUTH_STRATEGY to defaultServiceAccountAuthStrategy.id,
        KubeParametersConstants.API_SERVER_URL to "https://kubernetes.example.com"
    )

    companion object {
        private const val TOKEN = "service-account-token"
    }
}
