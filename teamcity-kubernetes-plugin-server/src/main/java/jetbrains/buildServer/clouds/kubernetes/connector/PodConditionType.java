/*
 * Copyright 2000-2022 JetBrains s.r.o.
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

package jetbrains.buildServer.clouds.kubernetes.connector;

import org.jetbrains.annotations.NotNull;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 29.06.17.
 */
public enum PodConditionType {
    PodScheduled,
    Ready,
    Initialized,
    Unschedulable,
    DisruptionTarget,
    Unknown;

    /**
     * Use this method as safer alternative to {@link PodConditionType#valueOf(String)}
     * @param conditionType - Pod's condition type. Not null value expected.
     * @return PodConditionType
     */
    @NotNull
    public static PodConditionType fromString(@NotNull String conditionType) {
        try {
            return PodConditionType.valueOf(conditionType);
        } catch (IllegalArgumentException e) {
            return Unknown;
        }
    }
}
