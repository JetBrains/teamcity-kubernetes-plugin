package jetbrains.buildServer.clouds.kubernetes.buildFeature

import jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants
import jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants.RUN_IN_KUBE_FEATURE
import jetbrains.buildServer.clouds.server.CloudManager
import jetbrains.buildServer.serverSide.*
import jetbrains.buildServer.util.EventDispatcher
import jetbrains.buildServer.web.openapi.PluginDescriptor
import java.util.*

class RunInKubeFeature (descriptor: PluginDescriptor,
                        private val cloudManager: CloudManager,
                        dispatcher: EventDispatcher<BuildServerListener>
                        ) : BuildFeature() {
    companion object {
        val FEATURE_PATH = "runInKubeFeature.jsp"
    }

    init{
        dispatcher.addListener(object : BuildServerAdapter(){
            override fun buildTypeAddedToQueue(queuedBuild: SQueuedBuild) {
                val buildEx = queuedBuild as QueuedBuildEx
                buildEx.buildType.getBuildFeaturesOfType(RUN_IN_KUBE_FEATURE).firstOrNull()?.apply {
                    // buildEx.buildPromotion.customParameters return new HashMap, so we can just our param in it
                    val customParameters = buildEx.buildPromotion.customParameters
                    customParameters.putIfAbsent(RUN_IN_KUBE_FEATURE, "true")
                    this.parameters.forEach { k, v ->  customParameters.putIfAbsent(k, v)}
                    buildEx.buildPromotion.customParameters = customParameters
                }
            }
        })
    }

    private val myEditUrl = descriptor.getPluginResourcesPath(FEATURE_PATH)

    override fun getType() = RUN_IN_KUBE_FEATURE

    override fun getDisplayName() = "Run in Kubernetes"

    override fun getEditParametersUrl() = myEditUrl

    fun showProfilesAndImages(project: SProject) : List<Pair<String, String>> {
        val retval = ArrayList<Pair<String, String>>()
        var p:SProject? = project
        do {
            val projectProfiles = cloudManager.listProfilesByProject(p!!.projectId, false)
            projectProfiles.forEach {
                val profileId = it.profileId
                val client = cloudManager.getClientIfExists(it.projectId, profileId)
                client?.images?.forEach {
                    retval.add(Pair("${p!!.projectId}:$profileId:${it.id}", it.name))
                }
            }
            p = p.parentProject
        } while (p != null)
        return retval
    }

    override fun isMultipleFeaturesPerBuildTypeAllowed() = false

    override fun describeParameters(params: MutableMap<String, String>): String {
        val imgParam = params[KubeParametersConstants.RUN_IN_KUBE_AGENT_SOURCE] ?: return "Not used"
        val split = imgParam.split(":")
        if (split.size < 3){
            return "Not used"
        }
        val imageId = imgParam.substring(split[0].length + split[1].length + 2)
        val profile = cloudManager.findProfileById(split[0], split[1]) ?: return "<Unknown Cloud profile>"
        val client = cloudManager.getClientIfExists(split[0], split[1]) ?: return "<Unknown Cloud profile>"

        for (image in client.images) {
            if (image.id == imageId){
                return "Profile: ${profile.profileName}\nImage: ${image.name}"
            }
        }

        return "<Unknown Cloud Image>"
    }

    override fun getDefaultParameters(): MutableMap<String, String> {
        return Collections.singletonMap(KubeParametersConstants.RUN_IN_KUBE_DOCKER_IMAGE, "jetbrains/teamcity-agent")
    }
}