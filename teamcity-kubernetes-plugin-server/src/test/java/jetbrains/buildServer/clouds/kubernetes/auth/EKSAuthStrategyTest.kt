package jetbrains.buildServer.clouds.kubernetes.auth

import com.intellij.openapi.util.Pair
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
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.util.concurrent.TimeUnit

class EKSAuthStrategyTest : BaseServerTestCase() {
    private lateinit var timeService: MockTimeService
    private lateinit var strategy: EKSAuthStrategy

    @BeforeMethod(alwaysRun = true)
    override fun setUp() {
        super.setUp()
        timeService = MockTimeService()
        strategy = EKSAuthStrategy(timeService, myProjectManager)
    }

    @AfterMethod(alwaysRun = true)
    override fun tearDown() {
        RefreshableStrategy.invalidateAll()
        super.tearDown()
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

    @Test
    fun testInstanceProfileIsRejectedWhenDisabled() {
        cacheToken(INSTANCE_PROFILE_KEY)

        thenThrownBy { strategy.apply(ConfigBuilder(), ParametersKubeApiConnection(instanceProfileParameters())) }
            .isInstanceOf(KubeCloudException::class.java)
            .hasMessageContaining(EKSAuthStrategy.INSTANCE_PROFILE_DISABLED_MESSAGE)
            .hasMessageNotContaining(EKSAuthStrategy.ENABLE_LOCAL_AWS_ACCOUNT)
    }

    @Test
    fun testInstanceProfileIsRejectedWhenDisabledEvenIfProjectAlreadyUsesIt() {
        myProject.addFeature(CloudConstants.CLOUD_PROFILE_FEATURE_TYPE, instanceProfileParameters())
        cacheToken(INSTANCE_PROFILE_KEY)

        thenThrownBy { strategy.apply(ConfigBuilder(), ParametersKubeApiConnection(instanceProfileParameters())) }
            .isInstanceOf(KubeCloudException::class.java)
            .hasMessageContaining(EKSAuthStrategy.INSTANCE_PROFILE_DISABLED_MESSAGE)
            .hasMessageNotContaining(EKSAuthStrategy.ENABLE_LOCAL_AWS_ACCOUNT)
    }

    @Test
    fun testInstanceProfileIsUsedWhenEnabled() {
        setInternalProperty(EKSAuthStrategy.ENABLE_LOCAL_AWS_ACCOUNT, "true")
        cacheToken(INSTANCE_PROFILE_KEY)

        then(strategy.apply(ConfigBuilder(), ParametersKubeApiConnection(instanceProfileParameters())).oauthToken)
            .isEqualTo(TOKEN)
    }

    @Test
    fun testAccessKeysAreUsedWhenInstanceProfileIsDisabled() {
        cacheToken(Pair.create(ACCESS_ID, CLUSTER_NAME))

        then(strategy.apply(ConfigBuilder(), ParametersKubeApiConnection(accessKeysParameters())).oauthToken)
            .isEqualTo(TOKEN)
    }

    @Test
    fun testSavingInstanceProfileIsRejectedWhenDisabled() {
        val invalidProperties = profilePropertiesProcessor().process(instanceProfileParameters())

        then(invalidProperties.map { it.propertyName }).containsExactly(KubeParametersConstants.EKS_USE_INSTANCE_PROFILE)
        then(invalidProperties.single().invalidReason).contains(EKSAuthStrategy.INSTANCE_PROFILE_DISABLED_MESSAGE).doesNotContain(EKSAuthStrategy.ENABLE_LOCAL_AWS_ACCOUNT)
    }

    @Test
    fun testSavingInstanceProfileIsAllowedWhenEnabled() {
        setInternalProperty(EKSAuthStrategy.ENABLE_LOCAL_AWS_ACCOUNT, "true")

        then(profilePropertiesProcessor().process(instanceProfileParameters())).isEmpty()
    }

    @Test
    fun testSavingAccessKeysIsAllowedWhenInstanceProfileIsDisabled() {
        then(profilePropertiesProcessor().process(accessKeysParameters())).isEmpty()
    }

    private fun isLocalEksAvailable(): Boolean {
        val map = mutableMapOf<String, Any>()
        strategy.fillAdditionalSettings(map, myProject.projectId, true)

        return map[KubeParametersConstants.EKS_USE_INSTANCE_PROFILE_ENABLED].toString().toBoolean()
    }

    /**
     * A cached token lets the strategy succeed without calling AWS
     */
    private fun cacheToken(key: Pair<String, String>) {
        RefreshableStrategy.CACHED_TOKENS[key] = Pair.create(TOKEN, timeService.now() + TimeUnit.MINUTES.toMillis(10))
    }

    private fun profilePropertiesProcessor() =
        KubeProfilePropertiesProcessor(KubeAuthStrategyProviderImpl(timeService, myProjectManager))

    private fun instanceProfileParameters() = mutableMapOf(
        KubeParametersConstants.AUTH_STRATEGY to strategy.id,
        KubeParametersConstants.API_SERVER_URL to "https://example.eks.amazonaws.com",
        KubeParametersConstants.EKS_CLUSTER_NAME to CLUSTER_NAME,
        KubeParametersConstants.EKS_USE_INSTANCE_PROFILE to "true"
    )

    private fun accessKeysParameters() = mutableMapOf(
        KubeParametersConstants.AUTH_STRATEGY to strategy.id,
        KubeParametersConstants.API_SERVER_URL to "https://example.eks.amazonaws.com",
        KubeParametersConstants.EKS_CLUSTER_NAME to CLUSTER_NAME,
        KubeParametersConstants.EKS_ACCESS_ID to ACCESS_ID,
        KubeAuthStrategy.SECURE_PREFIX + KubeParametersConstants.EKS_SECRET_KEY to "secret-key"
    )

    companion object {
        private const val TOKEN = "k8s-aws-v1.cached"
        private const val CLUSTER_NAME = "test-cluster"
        private const val ACCESS_ID = "test-access-id"
        private val INSTANCE_PROFILE_KEY: Pair<String, String> = Pair.create("instance-profile", CLUSTER_NAME)
    }
}
