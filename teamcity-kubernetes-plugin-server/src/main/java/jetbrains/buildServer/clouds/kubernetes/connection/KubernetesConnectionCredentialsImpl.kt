package jetbrains.buildServer.clouds.kubernetes.connection

import io.fabric8.kubernetes.client.Config

class KubernetesConnectionCredentialsImpl(private val myConfig: Config, private val myParameters: Map<String, String>) : KubernetesConnectionCredentials {
    public override fun getProperties(): Map<String, String> = myParameters

    public override fun getCredentials(): Config = myConfig
}
