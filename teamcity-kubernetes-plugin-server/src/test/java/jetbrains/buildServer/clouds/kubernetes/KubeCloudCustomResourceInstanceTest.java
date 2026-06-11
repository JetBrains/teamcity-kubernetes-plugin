package jetbrains.buildServer.clouds.kubernetes;

import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.clouds.CloudImageParameters;
import jetbrains.buildServer.clouds.InstanceStatus;
import jetbrains.buildServer.clouds.kubernetes.connector.FakeKubeApiConnector;
import jetbrains.buildServer.clouds.kubernetes.podSpec.CustomResourceTemplateProvider;
import jetbrains.buildServer.clouds.server.impl.profile.CloudImageDataImpl;
import jetbrains.buildServer.clouds.server.impl.profile.CloudImageParametersImpl;
import org.testng.annotations.Test;

import static org.assertj.core.api.BDDAssertions.then;

@Test
public class KubeCloudCustomResourceInstanceTest extends BaseTestCase {

    public void starts_scheduled_then_starting_without_conditions() {
        final KubeCloudCustomResourceInstance instance = createInstance();
        then(instance.getStatus()).isEqualTo(InstanceStatus.SCHEDULED_TO_START);

        instance.updateState(resource("vm-1", null));
        then(instance.getStatus()).isEqualTo(InstanceStatus.STARTING);
    }

    public void ready_condition_marks_running() {
        final KubeCloudCustomResourceInstance instance = createInstance();
        instance.updateState(resource("vm-1", condition("Ready", "True", null, null)));
        then(instance.getStatus()).isEqualTo(InstanceStatus.RUNNING);
        then(instance.getErrorInfo()).isNull();
    }

    public void agent_registration_running_is_not_downgraded() {
        final KubeCloudCustomResourceInstance instance = createInstance();
        instance.setStatus(InstanceStatus.RUNNING); // set when the agent registered
        instance.updateState(resource("vm-1", condition("Ready", "False", "Creating", null)));
        then(instance.getStatus()).isEqualTo(InstanceStatus.RUNNING);
    }

    public void synced_false_surfaces_error() {
        final KubeCloudCustomResourceInstance instance = createInstance();
        instance.updateState(resource("vm-1", condition("Synced", "False", "ReconcileError", "composition not found")));
        then(instance.getErrorInfo()).isNotNull();
        then(instance.getErrorInfo().getDetailedMessage()).contains("composition not found");
    }

    public void terminating_instance_status_is_not_overwritten() {
        final KubeCloudCustomResourceInstance instance = createInstance();
        instance.setStatus(InstanceStatus.SCHEDULED_TO_STOP);
        instance.updateState(resource("vm-1", condition("Ready", "True", null, null)));
        then(instance.getStatus()).isEqualTo(InstanceStatus.SCHEDULED_TO_STOP);
    }

    private KubeCloudCustomResourceInstance createInstance() {
        final Map<String, String> params = createMap(
          CloudImageParameters.SOURCE_ID_FIELD, "image1",
          KubeParametersConstants.POD_TEMPLATE_MODE, CustomResourceTemplateProvider.ID,
          KubeParametersConstants.AGENT_NAME_PREFIX, "vm-");
        final KubeCloudImage image = new KubeCloudImageImpl(
          new KubeCloudImageData(new CloudImageParametersImpl(new CloudImageDataImpl(params), "project123", "image1")),
          new FakeKubeApiConnector()
        );
        return new KubeCloudCustomResourceInstance(image, resource("vm-1", null));
    }

    private static GenericKubernetesResource resource(String name, Map<String, Object> condition) {
        final GenericKubernetesResource resource = new GenericKubernetesResource();
        resource.setApiVersion("smog.example.io/v1alpha1");
        resource.setKind("XSmogVM");
        final ObjectMeta metadata = new ObjectMeta();
        metadata.setName(name);
        resource.setMetadata(metadata);
        if (condition != null) {
            final Map<String, Object> status = new HashMap<>();
            status.put("conditions", Arrays.asList(condition));
            resource.setAdditionalProperty("status", status);
        }
        return resource;
    }

    private static Map<String, Object> condition(String type, String status, String reason, String message) {
        final Map<String, Object> condition = new HashMap<>();
        condition.put("type", type);
        condition.put("status", status);
        if (reason != null) condition.put("reason", reason);
        if (message != null) condition.put("message", message);
        return condition;
    }
}
