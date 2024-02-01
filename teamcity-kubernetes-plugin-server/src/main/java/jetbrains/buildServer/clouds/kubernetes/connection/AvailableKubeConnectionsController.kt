package jetbrains.buildServer.clouds.kubernetes.connection

import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.serverSide.connections.ProjectConnectionsManager
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping(KubernetesConnectionConstants.AVAILABLE_CONNECTIONS_CONTROLLER)
class AvailableKubeConnectionsController(private val projectManager: ProjectManager, private val projectConnectionsManager: ProjectConnectionsManager) {
    @RequestMapping(method = [RequestMethod.GET], produces = ["application/json"])
    fun getAvailableConnections(@RequestParam(name = "projectId") projectId: String): List<KubeConnection> {
        val project = projectManager.findProjectByExternalId(projectId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Project with id $projectId not found")
        return projectConnectionsManager.getAvailableConnectionsOfType(project, KubernetesConnectionConstants.CONNECTION_TYPE).map {
            KubeConnection(it.id, it.displayName)
        }
    }

    data class KubeConnection(val id: String, val name: String)
}
