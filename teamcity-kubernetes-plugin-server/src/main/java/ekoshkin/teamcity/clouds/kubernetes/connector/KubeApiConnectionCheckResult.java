package ekoshkin.teamcity.clouds.kubernetes.connector;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 01.06.17.
 */
public class KubeApiConnectionCheckResult {
    private final String myMessage;
    private final boolean mySuccess;

    private KubeApiConnectionCheckResult(String message, boolean success) {

        myMessage = message;
        mySuccess = success;
    }

    public static KubeApiConnectionCheckResult ok(String message) {
        return new KubeApiConnectionCheckResult(message, true);
    }

    public static KubeApiConnectionCheckResult error(String message) {
        return new KubeApiConnectionCheckResult(message, false);
    }

    public String getMessage() {
        return myMessage;
    }

    public boolean isSuccess() {
        return mySuccess;
    }
}
