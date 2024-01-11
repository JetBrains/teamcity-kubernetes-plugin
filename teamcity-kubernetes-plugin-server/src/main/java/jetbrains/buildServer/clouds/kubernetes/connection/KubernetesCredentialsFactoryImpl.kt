package jetbrains.buildServer.clouds.kubernetes.connection

import io.fabric8.kubernetes.client.Config
import io.fabric8.kubernetes.client.ConfigBuilder
import jetbrains.buildServer.clouds.kubernetes.ParametersKubeApiConnection
import jetbrains.buildServer.clouds.kubernetes.KubeUtils
import jetbrains.buildServer.clouds.kubernetes.auth.KubeAuthStrategy
import jetbrains.buildServer.clouds.kubernetes.auth.KubeAuthStrategyProvider
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnection
import jetbrains.buildServer.serverSide.SProject
import jetbrains.buildServer.serverSide.connections.ConnectionDescriptor
import jetbrains.buildServer.serverSide.connections.credentials.ConnectionCredentials
import jetbrains.buildServer.serverSide.connections.credentials.ConnectionCredentialsException
import jetbrains.buildServer.util.StringUtil

class KubernetesCredentialsFactoryImpl(private val myAuthStrategyProvider: KubeAuthStrategyProvider) : KubernetesCredentialsFactory {
    public override fun createConfig(connectionSettings: KubeApiConnection, authStrategy: KubeAuthStrategy): Config {
        var configBuilder = ConfigBuilder()
            .withNamespace(connectionSettings.namespace)
            .withRequestTimeout(DEFAULT_REQUEST_TIMEOUT_MS)
            .withHttp2Disable(true)
            .withConnectionTimeout(DEFAULT_CONNECTION_TIMEOUT_MS)

        if (authStrategy.requiresServerUrl()) {
            configBuilder.withMasterUrl(connectionSettings.apiServerUrl)
        }

        val caCertData = connectionSettings.caCertData
        if (StringUtil.isEmptyOrSpaces(caCertData)) {
            configBuilder.withTrustCerts(true)
        } else {
            configBuilder.withCaCertData(KubeUtils.encodeBase64IfNecessary(caCertData!!))
        }
        configBuilder = authStrategy.apply(configBuilder, connectionSettings)
        return configBuilder.build()
    }


    @Deprecated("Deprecated in ConnectionCredentialsFactory")
    @Throws(ConnectionCredentialsException::class)
    public override fun requestCredentials(connectionDescriptor: ConnectionDescriptor): ConnectionCredentials {
        val kubeApiConnection: KubeApiConnection = ParametersKubeApiConnection(connectionDescriptor.parameters)
        val authStrategy =
            myAuthStrategyProvider.find(kubeApiConnection.authStrategy) ?: throw ConnectionCredentialsException("Received unknown auth strategy " + kubeApiConnection.authStrategy)

        return KubernetesConnectionCredentialsImpl(createConfig(kubeApiConnection, authStrategy), connectionDescriptor.parameters)
    }

    @Throws(ConnectionCredentialsException::class)
    public override fun requestCredentials(project: SProject, connectionDescriptor: ConnectionDescriptor): ConnectionCredentials {
        return requestCredentials(connectionDescriptor)
    }

    public override fun getType(): String = KubernetesConnectionConstants.CONNECTION_TYPE

    companion object {
        private val DEFAULT_CONNECTION_TIMEOUT_MS = 5 * 1000
        private val DEFAULT_REQUEST_TIMEOUT_MS = 15 * 1000
    }
}
