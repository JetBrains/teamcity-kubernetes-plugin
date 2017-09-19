package jetbrains.buildServer.clouds.kubernetes;

import jetbrains.buildServer.clouds.CloudImageParameters;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 28.05.17.
 */
public class KubeParametersConstants {
    public static final String API_SERVER_URL = "api-server-url";
    public static final String AUTH_STRATEGY = "authStrategy";
    public static final String KUBERNETES_NAMESPACE = "kubernetes-namespace";
    public static final String PROFILE_INSTANCE_LIMIT = "profileInstanceLimit";
    public static final String IMAGE_INSTANCE_LIMIT = "imageInstanceLimit";
    public static final String POD_TEMPLATE_MODE = "podTemplateMode";
    public static final String DOCKER_IMAGE = "dockerImage";
    public static final String DOCKER_ARGS = "dockerArgs";
    public static final String DOCKER_CMD = "dockerCmd";
    public static final String IMAGE_PULL_POLICY = "imagePullPolicy";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String CLIENT_CERTIFICATE_DATA = "clientCertData";
    public static final String AUTH_TOKEN = "authToken";
    public static final String SOURCE_DEPLOYMENT = "sourceDeployment";

    public String getApiServerUrl() {
        return API_SERVER_URL;
    }

    public String getKubernetesNamespace() {
        return KUBERNETES_NAMESPACE;
    }

    public String getProfileInstanceLimit() {
        return PROFILE_INSTANCE_LIMIT;
    }

    public String getAgentPoolIdField() {
        return CloudImageParameters.AGENT_POOL_ID_FIELD;
    }

    public String getDockerImage() {
        return DOCKER_IMAGE;
    }

    public String getDockerArguments() {
        return DOCKER_ARGS;
    }

    public String getDockerCommand() {
        return DOCKER_CMD;
    }

    public String getImagePullPolicy() {
        return IMAGE_PULL_POLICY;
    }

    public String getAuthStrategy() {
        return AUTH_STRATEGY;
    }

    public String getUsername() {
        return USERNAME;
    }

    public String getPassword() {
        return PASSWORD;
    }

    public String getAuthToken() {
        return AUTH_TOKEN;
    }

    public String getClientCertData() {
        return CLIENT_CERTIFICATE_DATA;
    }

    public String getPodSpecMode() {
        return POD_TEMPLATE_MODE;
    }

    public String getSourceDeployment() {
        return SOURCE_DEPLOYMENT;
    }

    public String getMaxInstances() {
        return IMAGE_INSTANCE_LIMIT;
    }
}
