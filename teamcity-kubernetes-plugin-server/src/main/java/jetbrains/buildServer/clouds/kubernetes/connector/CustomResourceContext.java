
package jetbrains.buildServer.clouds.kubernetes.connector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.fabric8.kubernetes.client.dsl.base.ResourceDefinitionContext;
import java.io.IOException;
import java.util.Objects;
import jetbrains.buildServer.clouds.kubernetes.KubeCloudException;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Identifies a custom resource type (group/version/kind/plural + scope) so the
 * connector can address arbitrary custom resources (e.g. an XSmogVM Crossplane
 * composite in a KCP workspace) without compile-time model classes.
 */
public class CustomResourceContext {
    @NotNull private final String myGroup;
    @NotNull private final String myVersion;
    @NotNull private final String myKind;
    @NotNull private final String myPlural;
    private final boolean myClusterScoped;

    public CustomResourceContext(@NotNull String group,
                                 @NotNull String version,
                                 @NotNull String kind,
                                 @Nullable String plural,
                                 boolean clusterScoped) {
        myGroup = group;
        myVersion = version;
        myKind = kind;
        myPlural = StringUtil.isEmptyOrSpaces(plural) ? defaultPlural(kind) : plural.trim().toLowerCase();
        myClusterScoped = clusterScoped;
    }

    /**
     * Derives the resource type from a YAML manifest's apiVersion/kind fields.
     */
    @NotNull
    public static CustomResourceContext fromTemplate(@NotNull String templateYaml,
                                                     boolean clusterScoped,
                                                     @Nullable String pluralOverride) {
        final JsonNode root;
        try {
            root = new ObjectMapper(new YAMLFactory()).readTree(templateYaml);
        } catch (IOException e) {
            throw new KubeCloudException("Custom resource template is not valid YAML: " + e.getMessage(), e);
        }
        if (root == null || root.get("apiVersion") == null || root.get("kind") == null) {
            throw new KubeCloudException("Custom resource template must specify both 'apiVersion' and 'kind'");
        }
        final String apiVersion = root.get("apiVersion").asText();
        final String kind = root.get("kind").asText();
        final int slash = apiVersion.indexOf('/');
        final String group = slash < 0 ? "" : apiVersion.substring(0, slash);
        final String version = slash < 0 ? apiVersion : apiVersion.substring(slash + 1);
        if (StringUtil.isEmptyOrSpaces(version) || StringUtil.isEmptyOrSpaces(kind)) {
            throw new KubeCloudException("Custom resource template has empty 'apiVersion' or 'kind'");
        }
        return new CustomResourceContext(group, version, kind, pluralOverride, clusterScoped);
    }

    @NotNull
    private static String defaultPlural(@NotNull String kind) {
        final String lower = kind.toLowerCase();
        if (lower.endsWith("s") || lower.endsWith("x") || lower.endsWith("z") || lower.endsWith("ch") || lower.endsWith("sh")) {
            return lower + "es";
        }
        if (lower.endsWith("y") && lower.length() > 1 && "aeiou".indexOf(lower.charAt(lower.length() - 2)) < 0) {
            return lower.substring(0, lower.length() - 1) + "ies";
        }
        return lower + "s";
    }

    @NotNull
    public ResourceDefinitionContext toResourceDefinitionContext() {
        return new ResourceDefinitionContext.Builder()
          .withGroup(StringUtil.isEmpty(myGroup) ? null : myGroup)
          .withVersion(myVersion)
          .withKind(myKind)
          .withPlural(myPlural)
          .withNamespaced(!myClusterScoped)
          .build();
    }

    @NotNull
    public String getGroup() {
        return myGroup;
    }

    @NotNull
    public String getVersion() {
        return myVersion;
    }

    @NotNull
    public String getKind() {
        return myKind;
    }

    @NotNull
    public String getPlural() {
        return myPlural;
    }

    @NotNull
    public String getApiVersion() {
        return StringUtil.isEmpty(myGroup) ? myVersion : myGroup + "/" + myVersion;
    }

    public boolean isClusterScoped() {
        return myClusterScoped;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomResourceContext)) return false;
        final CustomResourceContext that = (CustomResourceContext)o;
        return myClusterScoped == that.myClusterScoped &&
               myGroup.equals(that.myGroup) &&
               myVersion.equals(that.myVersion) &&
               myKind.equals(that.myKind) &&
               myPlural.equals(that.myPlural);
    }

    @Override
    public int hashCode() {
        return Objects.hash(myGroup, myVersion, myKind, myPlural, myClusterScoped);
    }

    @Override
    public String toString() {
        return "CustomResourceContext{" + getApiVersion() + "/" + myKind + " (" + myPlural + "), clusterScoped=" + myClusterScoped + "}";
    }
}
