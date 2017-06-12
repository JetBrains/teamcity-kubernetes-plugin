package ekoshkin.teamcity.clouds.kubernetes.connector;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 13.06.17.
 */
public enum PodPhase {
    Pending,
    Running,
    Succeeded,
    Failed,
    Unknown
}
