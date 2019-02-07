package jetbrains.buildServer.clouds.kubernetes.auth

import com.intellij.openapi.util.Pair
import io.fabric8.kubernetes.client.ConfigBuilder
import jetbrains.buildServer.BaseTestCase
import jetbrains.buildServer.MockTimeService
import jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnection
import jetbrains.buildServer.util.TimeService
import org.assertj.core.api.BDDAssertions.then
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

@Test
class RefreshableStrategyTest : BaseTestCase() {
    private lateinit var apiConnection: KubeApiConnection
    private lateinit var customParameters: MutableMap<String, String>

    @BeforeMethod
    override public fun setUp(){
        super.setUp();
        customParameters = HashMap<String, String>()
        customParameters["DummyData"] = "DummyData"
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
        val strategy = object: DummyRefreshableStrategy(timeService){
            override fun retrieveNewToken(dataHolder: DummyData) = tokenRef.get()
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
        val strategy = object: DummyRefreshableStrategy(timeService){
            override fun retrieveNewToken(dataHolder: DummyData) = tokenRef.get()
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
        RefreshableStrategy.invalidateAll()
        super.tearDown()
    }
}

abstract class DummyRefreshableStrategy(myTimeService: TimeService) : RefreshableStrategy<DummyData>(myTimeService) {

    override fun createKey(dataHolder: DummyData) = Pair.create(dataHolder.data, dataHolder.data)

    override fun createData(connection: KubeApiConnection):DummyData {
        val data = connection.getCustomParameter("DummyData") ?: throw RuntimeException()
        return DummyData(data)
    }

    override fun getId() = "dummy"

    override fun getDisplayName() = "Dummy"

    override fun getDescription() = "Dummy description"
}


data class DummyData(val data : String){

}