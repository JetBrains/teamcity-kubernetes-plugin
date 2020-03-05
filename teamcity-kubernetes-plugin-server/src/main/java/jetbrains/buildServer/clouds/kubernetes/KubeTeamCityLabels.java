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

import io.fabric8.kubernetes.api.model.Pod;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 07.06.17.
 */
public class KubeTeamCityLabels {
    public static final String TEAMCITY_AGENT_LABEL = "teamcity-agent";
    public static final String TEAMCITY_SERVER_UUID = "teamcity-server-uuid";
    public static final String TEAMCITY_CLOUD_PROFILE = "teamcity-cloud-profile";
    public static final String TEAMCITY_CLOUD_IMAGE = "teamcity-cloud-image";
    public static final String POD_PVC_NAME = "pod-pvc-name";

    public static void addCustomLabel(@NotNull Pod pod,
                               @NotNull String label,
                               @NotNull String value){
        final Map<String, String> labels = new HashMap<>(pod.getMetadata().getLabels());
        labels.put(label, value);
        pod.getMetadata().setLabels(labels);
    }
}
