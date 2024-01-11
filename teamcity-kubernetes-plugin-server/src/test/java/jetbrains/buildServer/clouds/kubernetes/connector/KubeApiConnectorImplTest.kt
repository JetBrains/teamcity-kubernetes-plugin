/*
 * Copyright 2000-2022 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.clouds.kubernetes.connector

import com.intellij.openapi.util.Pair
import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.api.model.Status
import io.fabric8.kubernetes.client.*
import jetbrains.buildServer.BaseTestCase
import jetbrains.buildServer.MockTimeService
import jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants
import jetbrains.buildServer.clouds.kubernetes.auth.KubeAuthStrategyProviderImpl
import jetbrains.buildServer.clouds.kubernetes.auth.RefreshableStrategy
import jetbrains.buildServer.clouds.kubernetes.connection.KubernetesCredentialsFactoryImpl
import jetbrains.buildServer.serverSide.InvalidProperty
import jetbrains.buildServer.util.TimeService
import org.assertj.core.api.BDDAssertions.then
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test


@Test
class KubeApiConnectorImplTest : BaseTestCase() {

    private lateinit var apiConnection: KubeApiConnection
    private lateinit var customParameters: MutableMap<String, String>

    @BeforeMethod
    override fun setUp() {
        super.setUp()
        customParameters = HashMap<String, String>()
        customParameters["DummyData"] = "DummyData"
        apiConnection = object : KubeApiConnection {
            override fun getNamespace() = "test-namespace"

            override fun getCustomParameter(parameterName: String) = customParameters[parameterName]

            override fun getApiServerUrl() = "http://localhost:12345"

            override fun getCACertData() = null

            override fun getAuthStrategy() = AUTH_STRAGEGY
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
        val authStrategyProvider = KubeAuthStrategyProviderImpl(timeService)
        authStrategyProvider.registerStrategy(strategy)
        val kubeApiConnector = object: KubeApiConnectorImpl("kube-111", apiConnection, strategy, KubernetesCredentialsFactoryImpl(authStrategyProvider)){
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

    internal class FakeAuthStrategy(timeService: TimeService) : RefreshableStrategy<DummyData>(timeService) {
        override fun createKey(dataHolder: DummyData): Pair<String, String> {
            return Pair.create(dataHolder.data, dataHolder.data)
        }

        override fun createData(connection: KubeApiConnection): DummyData {
            return DummyData(connection.getCustomParameter("DummyData")!!)
        }

        override fun getId() = AUTH_STRAGEGY

        override fun getDisplayName() = "Dummy Display Name"

        override fun getDescription() = "Dummy Description"

        var retrieveCnt = 0;

        override fun retrieveNewToken(dataHolder: DummyData): Pair<String, Long>? {
            retrieveCnt++
            return Pair.create("Token-$retrieveCnt", 100)
        }

        override fun process(properties: MutableMap<String, String>?): MutableCollection<InvalidProperty> {
            return arrayListOf()
        }
    }

    @AfterMethod
    override fun tearDown() {
        RefreshableStrategy.invalidateAll()
        super.tearDown()
    }

    companion object {
        private const val AUTH_STRAGEGY = "dummy"
    }
}

data class DummyData(val data: String) {
}