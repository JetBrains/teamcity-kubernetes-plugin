package jetbrains.buildServer.clouds.kubernetes.buildFeature

import jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants
import jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants.RUN_IN_KUBE_FEATURE
import jetbrains.buildServer.clouds.server.CloudManager
import jetbrains.buildServer.serverSide.BuildFeature
import jetbrains.buildServer.serverSide.SProject
import jetbrains.buildServer.web.openapi.PluginDescriptor

class RunInKubeFeature (descriptor: PluginDescriptor, private val cloudManager: CloudManager)
    : BuildFeature() {
    companion object {
        val FEATURE_PATH = "runInKubeFeature.jsp"
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
        val imgParam = params[KubeParametersConstants.RUN_IN_KUBE_PARAM] ?: return "Not used"
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
}