/*
 * Copyright 2000-2020 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.clouds.kubernetes;

import io.fabric8.kubernetes.api.model.PodStatus;
import java.util.HashMap;
import java.util.Map;
import jetbrains.buildServer.clouds.CloudErrorInfo;
import jetbrains.buildServer.clouds.InstanceStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 13.06.17.
 */
public class InstanceStatusUtils {

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
    public static CloudErrorInfo getErrorMessage(@NotNull PodStatus podStatus){
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

    static {
        myPhasesMap.put("Running", InstanceStatus.RUNNING);
        myPhasesMap.put("Pending", InstanceStatus.SCHEDULED_TO_START);
        myPhasesMap.put("Succeeded", InstanceStatus.STOPPED);
        myPhasesMap.put("Failed", InstanceStatus.STOPPED);
    }
}
