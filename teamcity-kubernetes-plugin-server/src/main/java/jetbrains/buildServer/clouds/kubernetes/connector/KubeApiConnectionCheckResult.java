package jetbrains.buildServer.clouds.kubernetes.connector;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 01.06.17.
 */
public class KubeApiConnectionCheckResult {
    private final String myMessage;
    private final boolean mySuccess;
    private final boolean myNeedRefresh;

    private KubeApiConnectionCheckResult(String message, boolean success, boolean needRefresh) {
        myMessage = message;
        mySuccess = success;
        myNeedRefresh = needRefresh;
    }

    public static KubeApiConnectionCheckResult ok(String message) {
        return new KubeApiConnectionCheckResult(message, true, false);
    }

    public static KubeApiConnectionCheckResult error(String message, boolean needRefresh) {
        return new KubeApiConnectionCheckResult(message, false, needRefresh);
    }

    public String getMessage() {
        return myMessage;
    }

    public boolean isSuccess() {
        return mySuccess;
    }

    public boolean isNeedRefresh() {
        return myNeedRefresh;
    }
}
