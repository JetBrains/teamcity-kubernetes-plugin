package ekoshkin.teamcity.clouds.kubernetes.connector;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 29.06.17.
 */
public enum PodConditionType {
    PodScheduled,
    Ready,
    Initialized,
    Unschedulable
}
