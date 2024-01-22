package jetbrains.buildServer.clouds.kubernetes.connection

import jetbrains.buildServer.serverSide.connections.ConnectionDescriptor
import jetbrains.buildServer.serverSide.connections.ProjectConnectionsManager
import jetbrains.buildServer.serverSide.impl.BaseServerTestCase
import org.junit.Assert
import org.mockito.Mockito
import org.springframework.web.server.ResponseStatusException
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class AvailableKubeConnectionsControllerTest : BaseServerTestCase() {
    private lateinit var projectConnectionsManager: ProjectConnectionsManager
    private lateinit var availableKubeConnectionsController: AvailableKubeConnectionsController

    @BeforeMethod
    override fun setUp() {
        super.setUp()
        projectConnectionsManager = Mockito.mock(ProjectConnectionsManager::class.java)
        availableKubeConnectionsController = AvailableKubeConnectionsController(myProjectManager, projectConnectionsManager)
    }

    private fun getDescriptor(): ConnectionDescriptor {
        val descriptor = Mockito.mock(ConnectionDescriptor::class.java)
        Mockito.`when`(descriptor.id).thenReturn(ID)
        Mockito.`when`(descriptor.displayName).thenReturn(DISPLAY_NAME)

        return descriptor
    }

    @Test
    fun testGetAvailableConnections() {
        val descriptor = getDescriptor()
        Mockito.`when`(projectConnectionsManager.getAvailableConnectionsOfType(myProject, KubernetesConnectionConstants.CONNECTION_TYPE)).thenReturn(listOf(descriptor))

        val connections = availableKubeConnectionsController.getAvailableConnections(myProject.externalId)
        Assert.assertTrue(connections.size == 1)
        val connection = connections.first()
        Assert.assertTrue(connection.name == DISPLAY_NAME)
        Assert.assertTrue(connection.id == ID)
    }

    @Test(expectedExceptions = [ResponseStatusException::class])
    fun `test sending unexistent project id`() {
        availableKubeConnectionsController.getAvailableConnections("fake id")
    }


    companion object {
        private const val ID = "mockId"
        private const val DISPLAY_NAME = "displayName"
    }
}