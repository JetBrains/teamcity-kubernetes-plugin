package ekoshkin.teamcity.clouds.kubernetes;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 28.05.17.
 */
public class KubeConstants {
    private static final String API_SERVER_URL = "api-server-url";
    private static final String SERVICE_ACCOUNT_NAME = "service-account-name";
    private static final String SERVICE_ACCOUNT_TOKEN = "service-account-token";
    private static final String KUBERNETES_NAMESPACE = "kubernetes-namespace";

    public String getApiServerUrl() {
        return API_SERVER_URL;
    }

    public String getSeviceAccountName() {
        return SERVICE_ACCOUNT_NAME;
    }

    public String getSeviceAccountToken() {
        return SERVICE_ACCOUNT_TOKEN;
    }

    public String getKubernetesNamespace() {
        return KUBERNETES_NAMESPACE;
    }
}
