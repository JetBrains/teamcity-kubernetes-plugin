package jetbrains.buildServer.clouds.kubernetes.buildFeature

import jetbrains.buildServer.agent.Constants
import jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants
import jetbrains.buildServer.serverSide.QueuedBuildEx
import jetbrains.buildServer.serverSide.SBuildAgent
import jetbrains.buildServer.serverSide.buildDistribution.AgentsFilterContext
import jetbrains.buildServer.serverSide.buildDistribution.AgentsFilterResult
import jetbrains.buildServer.serverSide.buildDistribution.SimpleWaitReason
import jetbrains.buildServer.serverSide.buildDistribution.StartingBuildAgentsFilter
import jetbrains.buildServer.util.StringUtil

class RunInKubeAgentsFilter : StartingBuildAgentsFilter {

    override fun filterAgents(context: AgentsFilterContext): AgentsFilterResult {
        val build = context.startingBuild as QueuedBuildEx
        val featureVal = build.buildPromotion.getParameter(KubeParametersConstants.RUN_IN_KUBE_FEATURE)?.value
        val result = AgentsFilterResult()
        val suitableAgent = ArrayList<SBuildAgent>()
        context.agentsForStartingBuild.forEach {
            val buildId = it.buildParameters[Constants.ENV_PREFIX + KubeParametersConstants.RUN_IN_KUBE_ENV]
            if (featureVal == "true") {
                if (buildId == build.buildPromotionInfo.id.toString()) {
                    suitableAgent.add(it)
                }
            } else if (StringUtil.isEmpty(buildId)){
                suitableAgent.add(it)
            }
        }
        if (suitableAgent.size == 0){
            if (featureVal == "true") {
                result.waitReason = SimpleWaitReason("The agent for this build is not ready yet")
            } else {
                result.waitReason = SimpleWaitReason("There are no free agents")
            }
        }
        result.filteredConnectedAgents = suitableAgent
        return result
    }
}