package jetbrains.buildServer.clouds.kubernetes;

import jetbrains.buildServer.clouds.CloudImageParameters;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 28.05.17.
 */
public class KubeParametersConstants {
    public static final String DEFAULT_NAMESPACE = "default";

    public static final String API_SERVER_URL = "api-server-url";
    public static final String AUTH_STRATEGY = "authStrategy";
    public static final String KUBERNETES_NAMESPACE = "kubernetes-namespace";
    public static final String CA_CERT_DATA = "ca-cert-data";
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
    public static final String OIDC_CLIENT_ID = "oidcClientId";
    public static final String OIDC_CLIENT_SECRET = "oidcClientSecret";
    public static final String OIDC_REFRESH_TOKEN = "oidcRefreshToken";
    public static final String OIDC_ISSUER_URL = "issuer_url";
    public static final String CUSTOM_POD_TEMPLATE = "customPodTemplate";
    public static final String SOURCE_DEPLOYMENT = "sourceDeployment";
    public static final String AGENT_NAME_PREFIX = "agentNamePrefix";

    public static final String EKS_ACCESS_ID = "eksAccessId";
    public static final String EKS_SECRET_KEY = "eksSecretKey";
    public static final String EKS_REGION = "eksRegion";
    public static final String EKS_CLUSTER_NAME = "eksClusterName";

    public static final String RUN_IN_KUBE_FEATURE = "RunInKubernetes";
    public static final String RUN_IN_KUBE_PARAM = "runInKube";


    public String getApiServerUrl() {
        return API_SERVER_URL;
    }

    public String getCaCertData() {
        return CA_CERT_DATA;
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

    public String getOidcClientId() {
        return OIDC_CLIENT_ID;
    }

    public String getOidcClientSecret() {
        return OIDC_CLIENT_SECRET;
    }

    public String getOidcRefreshToken() {
        return OIDC_REFRESH_TOKEN;
    }

    public String getOidcIssuerUrl() {
        return OIDC_ISSUER_URL;
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

    public String getCustomPodTemplate() {
        return CUSTOM_POD_TEMPLATE;
    }

    public String getMaxInstances() {
        return IMAGE_INSTANCE_LIMIT;
    }

    public String getAgentNamePrefix() {
        return AGENT_NAME_PREFIX;
    }

    public String getEksAccessId() {
        return EKS_ACCESS_ID;
    }

    public String getEksSecretKey() {
        return EKS_SECRET_KEY;
    }

    public String getEksRegion() {
        return EKS_REGION;
    }

    public String getEksClusterName() {
        return EKS_CLUSTER_NAME;
    }

    public String getRunInKubeParam(){
        return RUN_IN_KUBE_PARAM;
    }
}
