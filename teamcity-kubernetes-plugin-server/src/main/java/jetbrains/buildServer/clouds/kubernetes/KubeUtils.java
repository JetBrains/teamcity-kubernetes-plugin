
package jetbrains.buildServer.clouds.kubernetes;

import io.fabric8.kubernetes.api.model.PodStatus;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import jetbrains.buildServer.clouds.CloudErrorInfo;
import jetbrains.buildServer.clouds.InstanceStatus;
import jetbrains.buildServer.util.StringUtil;
import org.apache.commons.codec.binary.Base64;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 13.06.17.
 */
public class KubeUtils {

    private static final Map<String, InstanceStatus> myPhasesMap = new HashMap<>();

    @NotNull
    public static InstanceStatus mapPodPhase(@NotNull PodStatus podStatus) {
        if (podStatus == null){
            return InstanceStatus.SCHEDULED_TO_START;
        }
        final String phase = podStatus.getPhase();
        return myPhasesMap.getOrDefault(phase, InstanceStatus.UNKNOWN);
    }

    @Nullable
    public static CloudErrorInfo getErrorMessage(@Nullable PodStatus podStatus){
        if (podStatus == null)
            return null;

        if (!podStatus.getPhase().equalsIgnoreCase("Failed")){
            return null;
        }
        final String message = podStatus.getMessage();
        final String reason = podStatus.getReason();
        if (message == null && reason == null){
            return new CloudErrorInfo("Unknown error occurred");
        }
        if (message == null){
            return new CloudErrorInfo(reason);
        }
        if (reason == null){
            return new CloudErrorInfo(message);
        }
        return new CloudErrorInfo(String.format("%s:%s", reason, message));
    }

    public static boolean isPodStatus(@NotNull InstanceStatus status){
        return myPhasesMap.containsValue(status);
    }

    @Nullable
    public static String escapeForKube(@Nullable String value){
        if (value == null){
            return null;
        }
        return value.replaceAll("[^A-Za-z0-9\\-]", "-");
    }

    public static String encodeBase64IfNecessary(@NotNull String dataString){
        byte[] decodedString;
        try {
            decodedString = Base64.decodeBase64(dataString);
        } catch (Exception ex) {
            decodedString = null;
        }
        if (StringUtil.areEqual(dataString, Base64.encodeBase64String(decodedString)))
            return dataString;
        else
            return Base64.encodeBase64String(dataString.getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] decodeBase64IfNecessary(@NotNull String dataString){
        byte[] decodedString;
        try {
            decodedString = Base64.decodeBase64(dataString);
        } catch (Exception ex) {
            decodedString = null;
        }
        if (StringUtil.areEqual(dataString, Base64.encodeBase64String(decodedString)))
            return Base64.decodeBase64(dataString);
        else
            return dataString.getBytes(StandardCharsets.UTF_8);
    }

    static {
        myPhasesMap.put("Running", InstanceStatus.RUNNING);
        myPhasesMap.put("Pending", InstanceStatus.SCHEDULED_TO_START);
        myPhasesMap.put("Succeeded", InstanceStatus.STOPPED);
        myPhasesMap.put("Failed", InstanceStatus.STOPPED);
    }
}