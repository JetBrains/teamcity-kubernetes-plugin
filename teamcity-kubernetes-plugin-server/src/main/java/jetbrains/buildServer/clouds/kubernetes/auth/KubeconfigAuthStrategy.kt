package jetbrains.buildServer.clouds.kubernetes.auth

import com.intellij.openapi.diagnostic.Logger
import io.fabric8.kubernetes.api.model.NamedContext
import io.fabric8.kubernetes.client.Config
import io.fabric8.kubernetes.client.ConfigBuilder
import jetbrains.buildServer.clouds.kubernetes.KubeCloudException
import jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnection
import jetbrains.buildServer.serverSide.InvalidProperty
import jetbrains.buildServer.serverSide.TeamCityProperties
import jetbrains.buildServer.util.FileUtil
import org.springframework.web.servlet.ModelAndView
import java.io.File

/**
@author Sergey.Pak
Date: 18.02.22
 */
class KubeconfigAuthStrategy() : KubeAuthStrategy {
    private val LOG = Logger.getInstance(KubeconfigAuthStrategy::class.java.name)

    companion object{
        const val ID = "kubeconfig"
        init{
            // disable autoconfiguration by default
            System.setProperty("kubernetes.disable.autoConfig", "true")
//            System.setProperty("kubernetes.auth.tryKubeConfig", "false")
        }
    }

    override fun getId() = ID

    override fun getDisplayName() = "Kubeconfig (local)"

    override fun getDescription() = "Take info from the kubeconfig on the server"

    //just return the default config
    override fun apply(clientConfig: ConfigBuilder, connection: KubeApiConnection): ConfigBuilder {
        // =nullIfEmpty, because Config.fromKubeconfig requires contextName to be empty to use the currentContext
        val contextName = connection.getCustomParameter(KubeParametersConstants.KUBECONFIG_CONTEXT).let {
            if (it.isNullOrEmpty())
                null
            else
                it
        }
        val kubeconfigFilename = Config.getKubeconfigFilename()
        if (kubeconfigFilename.isNullOrEmpty()) {
            throw KubeCloudException("cannot find kubeconfig file")
        }
        val contents = FileUtil.readText(File(kubeconfigFilename))

        // will ignore all other settings
        val conf = Config.fromKubeconfig(contextName, contents, kubeconfigFilename)
        return ConfigBuilder(conf)
    }


    override fun process(properties: MutableMap<String, String>?): MutableCollection<InvalidProperty>  = mutableListOf()

    override fun fillModel(mv: ModelAndView) {
        val config = Config.autoConfigure(null)
        val model = mv.model
        try {
            val contextsNames = config.contexts.map { it.name }
            val currentContext = config.currentContext
            model.put("contextNames", contextsNames)
            model.put("currentContext", currentContext?.name ?: "")
        } catch (ex: Exception) {
            LOG.warnAndDebugDetails("Error listing kubeconfig contexts", ex)
            model.put("kubeconfig-error", ex.toString())
        }
    }
}
