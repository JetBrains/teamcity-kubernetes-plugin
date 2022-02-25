package jetbrains.buildServer.clouds.kubernetes.auth

import com.intellij.openapi.diagnostic.Logger
import io.fabric8.kubernetes.client.Config
import io.fabric8.kubernetes.client.ConfigBuilder
import io.fabric8.kubernetes.client.internal.KubeConfigUtils
import jetbrains.buildServer.clouds.kubernetes.KubeCloudException
import jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnection
import jetbrains.buildServer.serverSide.InvalidProperty
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
        // will ignore all other settings
        val kubeconfigContent = readKubeconfigContent()
        val conf = Config.fromKubeconfig(contextName, kubeconfigContent, null)
        return ConfigBuilder(conf)
    }

    private fun readKubeconfigContent(): String? {
        val kubeconfigFilename: String?
        try {
            kubeconfigFilename = Config.getKubeconfigFilename()
        } catch (th: Throwable) {
            return null
        }
        if (kubeconfigFilename.isNullOrEmpty()) {
            return null
        }
        val file = File(kubeconfigFilename)
        return if (file.exists()) {
            FileUtil.readText(file)
        } else {
            null
        }
    }

    override fun process(properties: MutableMap<String, String>?): MutableCollection<InvalidProperty>  = mutableListOf()

    override fun fillModel(mv: ModelAndView) {
        val model = mv.model
        val contextsNames = arrayListOf<String>()
        var currentContextName = ""
        try {
            val content = readKubeconfigContent()
            if (!content.isNullOrEmpty()) {
                val config = KubeConfigUtils.parseConfigFromString(content)
                contextsNames.addAll(config.contexts.map{it.name})
                currentContextName = config.currentContext
            };
        } catch (ex: Exception) {
            LOG.warnAndDebugDetails("Error listing kubeconfig contexts", ex)
            model.put("kubeconfig-error", ex.toString())
        } finally {
            model.put("contextNames", contextsNames)
            model.put("currentContext", currentContextName)
        }
    }
}
