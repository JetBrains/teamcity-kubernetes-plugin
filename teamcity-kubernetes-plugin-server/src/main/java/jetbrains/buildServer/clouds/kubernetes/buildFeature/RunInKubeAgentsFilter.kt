/*
 * Copyright 2000-2021 JetBrains s.r.o.
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

package jetbrains.buildServer.clouds.kubernetes.buildFeature

import jetbrains.buildServer.agent.Constants
import jetbrains.buildServer.clouds.kubernetes.KubeContainerEnvironment
import jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants
import jetbrains.buildServer.serverSide.BuildPromotionManager
import jetbrains.buildServer.serverSide.QueuedBuildEx
import jetbrains.buildServer.serverSide.SBuildAgent
import jetbrains.buildServer.serverSide.buildDistribution.AgentsFilterContext
import jetbrains.buildServer.serverSide.buildDistribution.AgentsFilterResult
import jetbrains.buildServer.serverSide.buildDistribution.SimpleWaitReason
import jetbrains.buildServer.serverSide.buildDistribution.StartingBuildAgentsFilter
import jetbrains.buildServer.util.StringUtil

class RunInKubeAgentsFilter(private val buildPromotionManager: BuildPromotionManager) : StartingBuildAgentsFilter {

    override fun filterAgents(context: AgentsFilterContext): AgentsFilterResult {
        val promotionId = context.startingBuild.buildPromotionInfo.id
        val promotion = buildPromotionManager.findPromotionById(promotionId)
        val featureVal = promotion?.parameters?.get(KubeParametersConstants.RUN_IN_KUBE_FEATURE)
        val result = AgentsFilterResult()
        val suitableAgent = ArrayList<SBuildAgent>()
        context.agentsForStartingBuild.forEach {
            val buildId = it.buildParameters[Constants.ENV_PREFIX + KubeContainerEnvironment.BUILD_ID]
            if (featureVal == "true") {
                if (buildId == promotionId.toString()) {
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