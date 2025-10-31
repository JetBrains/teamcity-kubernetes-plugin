package jetbrains.buildServer.clouds.kubernetes.connection

import io.fabric8.kubernetes.client.Config
import jetbrains.buildServer.clouds.kubernetes.auth.KubeAuthStrategy
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnection
import jetbrains.buildServer.serverSide.connections.credentials.ConnectionCredentialsFactory

interface KubernetesCredentialsFactory : ConnectionCredentialsFactory {
    fun createConfig(connectionSettings: KubeApiConnection, authStrategy: KubeAuthStrategy, projectId: String, profileId: String): Config
}
