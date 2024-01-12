
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