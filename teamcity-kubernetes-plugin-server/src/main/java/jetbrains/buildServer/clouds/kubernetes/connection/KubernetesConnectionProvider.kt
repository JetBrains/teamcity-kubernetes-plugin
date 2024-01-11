package jetbrains.buildServer.clouds.kubernetes.connection

import jetbrains.buildServer.clouds.kubernetes.KubeProfilePropertiesProcessor
import jetbrains.buildServer.clouds.kubernetes.auth.KubeAuthStrategyProvider
import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.serverSide.TeamCityProperties
import jetbrains.buildServer.serverSide.oauth.OAuthProvider
import jetbrains.buildServer.web.openapi.PluginDescriptor

class KubernetesConnectionProvider(private val myPluginDescriptor: PluginDescriptor, private val myStrategyProvider: KubeAuthStrategyProvider) : OAuthProvider() {
    public override fun getType(): String = KubernetesConnectionConstants.CONNECTION_TYPE

    public override fun getDisplayName(): String = "Kubernetes Connection"

    public override fun isAvailable(): Boolean = TeamCityProperties.getBoolean(KubernetesConnectionConstants.KUBERNETES_CONNECTION_FEATURE_FLAG)

    public override fun getPropertiesProcessor(): PropertiesProcessor = KubeProfilePropertiesProcessor(myStrategyProvider)

    public override fun describeConnection(connectionProperties: Map<String, String>): String = super.describeConnection(connectionProperties)

    public override fun getEditParametersUrl(): String = myPluginDescriptor.getPluginResourcesPath("editConnection.jsp")

    public override fun getDefaultProperties(): Map<String, String>? = super.getDefaultProperties()
}
