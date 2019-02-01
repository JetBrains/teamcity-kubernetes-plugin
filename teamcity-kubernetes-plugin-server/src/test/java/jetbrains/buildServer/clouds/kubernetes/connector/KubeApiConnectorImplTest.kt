package jetbrains.buildServer.clouds.kubernetes.connector

import com.intellij.openapi.util.Pair
import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.api.model.Status
import io.fabric8.kubernetes.client.*
import io.fabric8.kubernetes.client.dsl.NamespaceVisitFromServerGetWatchDeleteRecreateWaitApplicable
import jetbrains.buildServer.BaseTestCase
import jetbrains.buildServer.MockTimeService
import jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants
import jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants.OIDC_CLIENT_ID
import jetbrains.buildServer.clouds.kubernetes.auth.KubeAuthStrategy
import jetbrains.buildServer.clouds.kubernetes.auth.OIDCAuthStrategy
import jetbrains.buildServer.util.TimeService
import org.assertj.core.api.BDDAssertions.then
import org.jmock.Mock
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

@Test
class KubeApiConnectorImplTest : BaseTestCase() {

    private lateinit var apiConnection: KubeApiConnection
    private lateinit var customParameters: MutableMap<String, String>

    @BeforeMethod
    override fun setUp() {
        super.setUp()
        customParameters = HashMap<String, String>()
        customParameters[KubeParametersConstants.OIDC_CLIENT_ID] = "clientId"
        customParameters[KubeParametersConstants.OIDC_CLIENT_SECRET] = "secret"
        customParameters[KubeParametersConstants.OIDC_ISSUER_URL] = "issuer"
        customParameters[KubeParametersConstants.OIDC_REFRESH_TOKEN] = "refreshToken"
        apiConnection = object : KubeApiConnection {
            override fun getNamespace() = "test-namespace"

            override fun getCustomParameter(parameterName: String) = customParameters[parameterName]

            override fun getApiServerUrl() = "http://localhost:12345"

            override fun getCACertData() = null


        }

    }

    public fun check_connection_refreshed(){
        val initialClient = FakeKubeClient()
        val errorMessage = "Failure executing: GET at: https://localhost:12345/api/v1/namespaces/test-namespace/pods?labelSelector=teamcity-cloud-image%3D0,teamcity-cloud-profile%3Dkube-4. Message: Unauthorized! Token may have expired! Please log-in again. Unauthorized."
        val status = Status()
        status.code = 401
        status.message = errorMessage
        initialClient.myException = KubernetesClientException(errorMessage, 401, status)
        val timeService = MockTimeService()
        val strategy = FakeAuthStrategy(timeService)
        var clientsCreated = 0
        val kubeApiConnector = object: KubeApiConnectorImpl(apiConnection, strategy){
            override fun createClient(config: Config): KubernetesClient {
                clientsCreated++
                if (clientsCreated == 2){
                    initialClient.myException = null
                }
                then(config.oauthToken).isEqualTo("Token-$clientsCreated")
                return initialClient
            }

            override fun testConnection(): KubeApiConnectionCheckResult {
                return KubeApiConnectionCheckResult.error(errorMessage, true)
            }
        }
        // createClient was already called in constructor

        kubeApiConnector.createPod(Pod())
        then(clientsCreated).isEqualTo(2)
        then(strategy.retrieveCnt).isEqualTo(2)
    }

    internal class FakeAuthStrategy(timeService: TimeService) : OIDCAuthStrategy(timeService) {
        var retrieveCnt = 0;

        override fun retrieveNewToken(dataHolder: DataHolder): Pair<String, Long>? {
            retrieveCnt++
            return Pair.create("Token-$retrieveCnt", 100)
        }
    }

    @AfterMethod
    override fun tearDown() {
        OIDCAuthStrategy.invalidateAll()
        super.tearDown()
    }
}