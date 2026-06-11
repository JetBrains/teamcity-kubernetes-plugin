package jetbrains.buildServer.clouds.kubernetes;

import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import java.util.Collections;
import java.util.Map;
import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.clouds.CloudImageParameters;
import jetbrains.buildServer.clouds.CloudInstanceUserData;
import jetbrains.buildServer.clouds.kubernetes.connector.FakeKubeApiConnector;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnector;
import jetbrains.buildServer.clouds.kubernetes.podSpec.CustomResourceTemplateProvider;
import jetbrains.buildServer.clouds.server.impl.profile.CloudImageDataImpl;
import jetbrains.buildServer.clouds.server.impl.profile.CloudImageParametersImpl;
import jetbrains.buildServer.serverSide.ServerResponsibilityImpl;
import jetbrains.buildServer.serverSide.ServerSettings;
import jetbrains.buildServer.serverSide.impl.ServerSettingsImpl;
import jetbrains.buildServer.serverSide.impl.auth.SecurityContextImpl;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.BDDAssertions.then;

@SuppressWarnings("unchecked")
@Test
public class CustomResourceTemplateProviderTest extends BaseTestCase {
    private static final String PROJECT_ID = "project123";
    private static final String TEMPLATE =
      "apiVersion: smog.example.io/v1alpha1\n" +
      "kind: XSmogVM\n" +
      "metadata:\n" +
      "  labels:\n" +
      "    custom: label\n" +
      "spec:\n" +
      "  image: windows-golden\n" +
      "  userData: |\n" +
      "    serverUrl=%server.url%\n" +
      "    serverUuid=%server.uuid%\n" +
      "    profileId=%profile.id%\n" +
      "    imageId=%image.id%\n" +
      "    instanceId=%instance.id%\n" +
      "    agentName=%agent.name%\n";

    private CustomResourceTemplateProvider myProvider;
    private KubeApiConnector myApiConnector;

    @BeforeMethod
    @Override
    public void setUp() throws Exception {
        super.setUp();
        final ServerSettings serverSettings = new ServerSettingsImpl(new SecurityContextImpl(new ServerResponsibilityImpl())) {
            @NotNull
            @Override
            public String getServerUUID() {
                return "SERVER-UUID";
            }
        };
        myProvider = new CustomResourceTemplateProvider(serverSettings);
        myApiConnector = new FakeKubeApiConnector() {
            @Override
            public String getNamespace() {
                return "ns1";
            }
        };
    }

    public void substitutes_placeholders_and_injects_labels() {
        final KubeCloudImage image = createImage(false);

        final GenericKubernetesResource resource = myProvider.getResourceTemplate("vm-agent-1", createInstanceTag(), image, myApiConnector);

        then(resource.getApiVersion()).isEqualTo("smog.example.io/v1alpha1");
        then(resource.getKind()).isEqualTo("XSmogVM");
        then(resource.getMetadata().getName()).isEqualTo("vm-agent-1");
        then(resource.getMetadata().getNamespace()).isEqualTo("ns1");
        then(resource.getMetadata().getLabels())
          .containsEntry("custom", "label")
          .containsEntry(KubeTeamCityLabels.TEAMCITY_CLOUD_IMAGE, "image1")
          .containsEntry(KubeTeamCityLabels.TEAMCITY_CLOUD_PROFILE, "profile id")
          .containsEntry(KubeTeamCityLabels.TEAMCITY_SERVER_UUID, "SERVER-UUID")
          .containsKey(KubeTeamCityLabels.TEAMCITY_AGENT_LABEL);

        final Map<String, Object> spec = (Map<String, Object>)resource.getAdditionalProperties().get("spec");
        then(spec.get("image")).isEqualTo("windows-golden");
        final String userData = (String)spec.get("userData");
        then(userData)
          .contains("serverUrl=server address")
          .contains("serverUuid=SERVER-UUID")
          .contains("profileId=profile id")
          .contains("imageId=image1")
          .contains("instanceId=vm-agent-1")
          .contains("agentName=vm-vm-agent-1");
    }

    public void cluster_scoped_resource_gets_no_namespace() {
        final KubeCloudImage image = createImage(true);

        final GenericKubernetesResource resource = myProvider.getResourceTemplate("vm-agent-1", createInstanceTag(), image, myApiConnector);

        then(resource.getMetadata().getNamespace()).isNull();
    }

    private KubeCloudImage createImage(boolean clusterScoped) {
        final Map<String, String> params = createMap(
          CloudImageParameters.SOURCE_ID_FIELD, "image1",
          KubeParametersConstants.POD_TEMPLATE_MODE, CustomResourceTemplateProvider.ID,
          KubeParametersConstants.CUSTOM_RESOURCE_TEMPLATE, TEMPLATE,
          KubeParametersConstants.CUSTOM_RESOURCE_CLUSTER_SCOPED, String.valueOf(clusterScoped),
          KubeParametersConstants.AGENT_NAME_PREFIX, "vm-");
        return new KubeCloudImageImpl(
          new KubeCloudImageData(new CloudImageParametersImpl(new CloudImageDataImpl(params), PROJECT_ID, "image1")),
          myApiConnector
        );
    }

    private CloudInstanceUserData createInstanceTag() {
        return new CloudInstanceUserData("agent name", "auth token", "server address", null, "profile id", "profile description",
                                         Collections.singletonMap(KubeContainerEnvironment.STARTING_INSTANCE_ID_PARAM, "token"));
    }
}
