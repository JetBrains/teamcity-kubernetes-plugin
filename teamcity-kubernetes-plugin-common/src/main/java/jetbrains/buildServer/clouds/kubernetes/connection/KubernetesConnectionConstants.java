package jetbrains.buildServer.clouds.kubernetes.connection;

public interface KubernetesConnectionConstants {
  String CONNECTION_TYPE = "KubernetesConnection";

  String KUBERNETES_CONNECTION_FEATURE_FLAG = "teamcity.internal.kubernetes.connections.enabled";

  String DISPLAY_NAME_PARAM = "displayName";

  String AVAILABLE_CONNECTIONS_CONTROLLER = "/k8s/availableConnections";
}
