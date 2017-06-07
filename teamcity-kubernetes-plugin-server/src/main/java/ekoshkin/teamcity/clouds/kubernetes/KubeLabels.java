package ekoshkin.teamcity.clouds.kubernetes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 07.06.17.
 */
class KubeLabels {
    static final String TEAMCITY_AGENT_LABEL = "teamcity-agent";

    @NotNull
    static String getServerLabel(@Nullable String serverUUID){
        return "teamcity-server-" + (serverUUID == null ? "UNKNOWN" : serverUUID);
    }

    @NotNull
    static String getProfileLabel(@NotNull String profileId){
        return "teamcity-cloud-profile-" + profileId;
    }

    @NotNull
    static String getImageLabel(@NotNull String imageId){
        return "teamcity-cloud-image-" + imageId;
    }
}
