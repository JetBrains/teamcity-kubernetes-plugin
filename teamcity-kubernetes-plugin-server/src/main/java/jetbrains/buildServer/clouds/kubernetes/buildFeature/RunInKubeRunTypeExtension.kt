package jetbrains.buildServer.clouds.kubernetes.buildFeature

import jetbrains.buildServer.clouds.kubernetes.KubeContainerEnvironment
import jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants
import jetbrains.buildServer.clouds.server.CloudManager
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementType
import jetbrains.buildServer.serverSide.InvalidProperty
import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.serverSide.RunTypeExtension
import jetbrains.buildServer.serverSide.RunTypeRegistry
import java.util.*

class RunInKubeRunTypeExtension(private val cloudManager: CloudManager, private val runTypeRegistry: RunTypeRegistry) : RunTypeExtension() {

    override fun getEditRunnerParamsJspFilePath() = "empty.jsp"

    override fun getViewRunnerParamsJspFilePath() = "empty.jsp"

    override fun getDefaultRunnerProperties() = emptyMap<String, String>()

    override fun getRunTypes(): MutableCollection<String> {
        return runTypeRegistry.registeredRunTypes.map {it.type}.toMutableList()
    }

    override fun getRunnerPropertiesProcessor(): PropertiesProcessor? {
        return object: PropertiesProcessor {
            override fun process(properties: MutableMap<String, String>?): MutableCollection<InvalidProperty> {
                return Collections.emptyList()
            }
        }
    }

    override fun processRunnerRequirements(runParameters: MutableMap<String, String>, requirementsList: MutableList<Requirement>) {
        super.processRunnerRequirements(runParameters, requirementsList)

        if (runParameters[KubeParametersConstants.RUN_IN_KUBE_FEATURE] == "true") {
            requirementsList.clear() // drop all requirements - we supplied the image name
            val agentSource = runParameters[KubeParametersConstants.RUN_IN_KUBE_AGENT_SOURCE]
            val split = agentSource?.split(":")
            if (split?.size == 3){
                requirementsList.add(Requirement(
                        KubeContainerEnvironment.PROFILE_ID,
                        split[1],
                        RequirementType.EQUALS)
                )
                requirementsList.add(Requirement(
                        KubeContainerEnvironment.IMAGE_ID,
                        split[2],
                        RequirementType.EQUALS)
                )
            }
        }
    }
}