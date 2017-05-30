package ekoshkin.teamcity.clouds.kubernetes.connector;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 30.05.17.
 */
public class KubeObjectPatches {
    public static KubeObjectPatch forDeploymentReplicas(int replicasCount){
        return new KubeObjectPatch() {
            @Override
            public String toString() {
                return super.toString();
            }
        };
    }
}
