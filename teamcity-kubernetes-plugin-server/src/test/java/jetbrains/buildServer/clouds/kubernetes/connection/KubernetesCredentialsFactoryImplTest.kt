package jetbrains.buildServer.clouds.kubernetes.connection

import io.fabric8.kubernetes.client.Config
import jetbrains.buildServer.BaseTestCase
import jetbrains.buildServer.MockTimeService
import jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants
import jetbrains.buildServer.clouds.kubernetes.KubeUtils
import jetbrains.buildServer.clouds.kubernetes.auth.KubeAuthStrategy
import jetbrains.buildServer.clouds.kubernetes.auth.KubeAuthStrategyProviderImpl
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnection
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.serverSide.connections.ConnectionDescriptor
import jetbrains.buildServer.serverSide.connections.credentials.ConnectionCredentialsException
import jetbrains.buildServer.util.StringUtil
import org.mockito.Mockito
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import org.assertj.core.api.BDDAssertions.then
import org.testng.annotations.AfterMethod

class KubernetesCredentialsFactoryImplTest : BaseTestCase() {
    private lateinit var authStrategy: KubeAuthStrategy
    private lateinit var kubeAuthStrategyProvider: KubeAuthStrategyProviderImpl
    private lateinit var credentialsFactoryImpl: KubernetesCredentialsFactoryImpl

    @BeforeMethod
    override fun setUp() {
        super.setUp()
        authStrategy = Mockito.mock(KubeAuthStrategy::class.java)
        Mockito.`when`(authStrategy.id).thenReturn(STRAGEGY_NAME)
        Mockito.`when`(authStrategy.apply(Mockito.any(), Mockito.any())).then { it.arguments.first() }
        kubeAuthStrategyProvider = KubeAuthStrategyProviderImpl(MockTimeService(), Mockito.mock(ProjectManager::class.java))
        kubeAuthStrategyProvider.registerStrategy(authStrategy)

        credentialsFactoryImpl = KubernetesCredentialsFactoryImpl(kubeAuthStrategyProvider)
    }

    @AfterMethod
    override fun tearDown() {
        super.tearDown()
        Mockito.validateMockitoUsage()
    }


    private fun getConnectionSettings(namespace: String = NAMESPACE, apiServerUrl: String? = null, caCertData: String? = null, authStrategy: String = STRAGEGY_NAME): KubeApiConnection {
        val connection = Mockito.mock(KubeApiConnection::class.java)
        Mockito.`when`(connection.namespace).thenReturn(namespace)
        Mockito.`when`(connection.apiServerUrl).thenReturn(apiServerUrl)
        Mockito.`when`(connection.caCertData).thenReturn(caCertData)
        Mockito.`when`(connection.authStrategy).thenReturn(authStrategy)
        return connection
    }


    private fun getConnectionDescriptor(namespace: String = NAMESPACE, apiServerUrl: String? = null, caCertData: String? = null, authStrategy: String = STRAGEGY_NAME): ConnectionDescriptor {
        val connection = Mockito.mock(ConnectionDescriptor::class.java)
        val parameters = mapOf(
            KubeParametersConstants.API_SERVER_URL to apiServerUrl,
            KubeParametersConstants.KUBERNETES_NAMESPACE to namespace,
            KubeParametersConstants.CA_CERT_DATA to caCertData,
            KubeParametersConstants.AUTH_STRATEGY to authStrategy,
        )
        Mockito.`when`(connection.parameters).thenReturn(parameters)
        return connection
    }

    @DataProvider(name = "kubeApiValues")
    fun kubeApiValues() = arrayOf(
        arrayOf(API_SERVER_URL, null),// With server URL and no caCert
        arrayOf(null, CA_CERT_DATA),// Without serverUrl and caCert
    )

    @Test(dataProvider = "kubeApiValues")
    fun testCreateConfig(apiServerUrl: String?, caCertData: String?) {
        test(apiServerUrl, caCertData) {
            val connectionSettings = getConnectionSettings(apiServerUrl = apiServerUrl, caCertData = caCertData)
            credentialsFactoryImpl.createConfig(connectionSettings, authStrategy)
        }
    }

    @Test(dataProvider = "kubeApiValues")
    fun testRequestCredentials(apiServerUrl: String?, caCertData: String?){
        test(apiServerUrl, caCertData) {
            val credentials = credentialsFactoryImpl.requestCredentials(getConnectionDescriptor(apiServerUrl = apiServerUrl, caCertData = caCertData))
            then(credentials).isInstanceOf(KubernetesConnectionCredentials::class.java)
            (credentials as KubernetesConnectionCredentials).credentials
        }
    }

    @Test(expectedExceptions = arrayOf(ConnectionCredentialsException::class))
    fun `testRequestCredentials with no auth strategy`(){
        val descriptor = getConnectionDescriptor(authStrategy = "fake one")
        credentialsFactoryImpl.requestCredentials(descriptor)
    }

    private fun test(apiServerUrl: String?, caCertData: String?, getConfig : () -> Config) {
        val requiresServerUrl = if (!StringUtil.isEmpty(apiServerUrl)) {
            true
        } else {
            false
        }
        Mockito.`when`(authStrategy.requiresServerUrl()).thenReturn(requiresServerUrl)
        val config = getConfig()
        then(config.namespace).isEqualTo(NAMESPACE)
        if (!StringUtil.isEmpty(apiServerUrl)) {
            then(config.masterUrl).contains(apiServerUrl)
        }

        if (!StringUtil.isEmpty(caCertData)) {
            then(config.caCertData).isEqualTo(KubeUtils.encodeBase64IfNecessary(caCertData!!))
        } else {
            then(config.isTrustCerts).isEqualTo(true)
        }
    }

    companion object {
        private const val STRAGEGY_NAME = "authStrategy"
        private const val NAMESPACE = "namespace"
        private const val API_SERVER_URL = "https://apiServerUrl.com"
        private const val CA_CERT_DATA = "CA_CERT_DATA"
    }
}