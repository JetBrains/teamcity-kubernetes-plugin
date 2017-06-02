package ekoshkin.teamcity.clouds.kubernetes.connector;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 01.06.17.
 */
public class KubeApiConnectionSettings {
    private String myApiServerUrl;
    private final String myAccountName;
    private final String myAccountToken;

    public KubeApiConnectionSettings(String apiServerUrl, String accountName, String accountToken) {
        myApiServerUrl = apiServerUrl;
        myAccountName = accountName;
        myAccountToken = accountToken;
    }

    public String getApiServerUrl() {
        return myApiServerUrl;
    }

    public String getAccountToken() {
        return myAccountToken;
    }

    public String getAccountName() {
        return myAccountName;
    }
}
