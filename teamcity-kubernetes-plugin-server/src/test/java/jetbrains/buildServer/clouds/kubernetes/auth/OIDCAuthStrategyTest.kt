package jetbrains.buildServer.clouds.kubernetes.auth

import com.intellij.openapi.util.Pair
import io.fabric8.kubernetes.client.ConfigBuilder
import jetbrains.buildServer.BaseTestCase
import jetbrains.buildServer.MockTimeService
import jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnection
import org.assertj.core.api.BDDAssertions.then
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

@Test
class OIDCAuthStrategyTest : BaseTestCase() {
    private lateinit var apiConnection: KubeApiConnection
    private lateinit var customParameters: MutableMap<String, String>

    @BeforeMethod
    override public fun setUp(){
        super.setUp();
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

    fun invalidate_by_time(){
        val timeService = MockTimeService()
        val tokenRef = AtomicReference<Pair<String, Long>>()
        val strategy = object: OIDCAuthStrategy(timeService){
            override fun retrieveNewToken(dataHolder: DataHolder) = tokenRef.get()
        }
        tokenRef.set(Pair("Token1", 100))
        then(strategy.apply(ConfigBuilder(), apiConnection).oauthToken).isEqualTo("Token1")
        tokenRef.set(Pair("Token2", 100))
        then(strategy.apply(ConfigBuilder(), apiConnection).oauthToken).isEqualTo("Token1")
        timeService.inc(200, TimeUnit.SECONDS)
        then(strategy.apply(ConfigBuilder(), apiConnection).oauthToken).isEqualTo("Token2")
    }

    fun force_invalidate(){
        val timeService = MockTimeService()
        val tokenRef = AtomicReference<Pair<String, Long>>()
        val strategy = object: OIDCAuthStrategy(timeService){
            override fun retrieveNewToken(dataHolder: DataHolder) = tokenRef.get()
        }
        tokenRef.set(Pair("Token1", 100))
        then(strategy.apply(ConfigBuilder(), apiConnection).oauthToken).isEqualTo("Token1")
        tokenRef.set(Pair("Token2", 100))
        then(strategy.apply(ConfigBuilder(), apiConnection).oauthToken).isEqualTo("Token1")
        strategy.invalidate(apiConnection)
        then(strategy.apply(ConfigBuilder(), apiConnection).oauthToken).isEqualTo("Token2")
    }


    @AfterMethod
    override fun tearDown() {
        OIDCAuthStrategy.invalidateAll()
        super.tearDown()
    }
}
