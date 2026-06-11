package jetbrains.buildServer.clouds.kubernetes.connector;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.clouds.kubernetes.KubeCloudException;
import org.testng.annotations.Test;

import static org.assertj.core.api.BDDAssertions.then;
import static org.testng.Assert.fail;

@Test
public class CustomResourceContextTest extends BaseTestCase {

    public void parse_group_version_kind_from_template() {
        final CustomResourceContext context = CustomResourceContext.fromTemplate(
          "apiVersion: smog.example.io/v1alpha1\n" +
          "kind: XSmogVM\n" +
          "spec:\n" +
          "  image: windows-golden\n",
          true, null);

        then(context.getGroup()).isEqualTo("smog.example.io");
        then(context.getVersion()).isEqualTo("v1alpha1");
        then(context.getKind()).isEqualTo("XSmogVM");
        then(context.getPlural()).isEqualTo("xsmogvms");
        then(context.getApiVersion()).isEqualTo("smog.example.io/v1alpha1");
        then(context.isClusterScoped()).isTrue();
    }

    public void parse_core_group() {
        final CustomResourceContext context = CustomResourceContext.fromTemplate(
          "apiVersion: v1\nkind: ConfigMap\n", false, null);

        then(context.getGroup()).isEmpty();
        then(context.getVersion()).isEqualTo("v1");
        then(context.getApiVersion()).isEqualTo("v1");
        then(context.isClusterScoped()).isFalse();
    }

    public void plural_override_wins() {
        final CustomResourceContext context = CustomResourceContext.fromTemplate(
          "apiVersion: smog.example.io/v1alpha1\nkind: XSmogVM\n", true, "XSmogVMs");

        then(context.getPlural()).isEqualTo("xsmogvms");
    }

    public void missing_kind_fails() {
        try {
            CustomResourceContext.fromTemplate("apiVersion: v1\n", false, null);
            fail("expected KubeCloudException");
        } catch (KubeCloudException e) {
            then(e.getMessage()).contains("kind");
        }
    }

    public void invalid_yaml_fails() {
        try {
            CustomResourceContext.fromTemplate("{{{not yaml", false, null);
            fail("expected KubeCloudException");
        } catch (KubeCloudException ignored) {
        }
    }

    public void resource_definition_context_is_built() {
        final CustomResourceContext context = CustomResourceContext.fromTemplate(
          "apiVersion: smog.example.io/v1alpha1\nkind: XSmogVM\n", true, null);
        final io.fabric8.kubernetes.client.dsl.base.ResourceDefinitionContext rdc = context.toResourceDefinitionContext();

        then(rdc.getGroup()).isEqualTo("smog.example.io");
        then(rdc.getVersion()).isEqualTo("v1alpha1");
        then(rdc.getPlural()).isEqualTo("xsmogvms");
        then(rdc.isNamespaceScoped()).isFalse();
    }
}
