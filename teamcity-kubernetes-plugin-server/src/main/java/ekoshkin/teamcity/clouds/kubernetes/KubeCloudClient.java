package ekoshkin.teamcity.clouds.kubernetes;

import jetbrains.buildServer.clouds.*;
import jetbrains.buildServer.serverSide.AgentDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 27.05.17.
 */
public class KubeCloudClient implements CloudClientEx {
    private CloudClientParameters myCloudClientParameters;

    public KubeCloudClient(CloudClientParameters cloudClientParameters) {
        myCloudClientParameters = cloudClientParameters;
    }

    @NotNull
    @Override
    public CloudInstance startNewInstance(@NotNull CloudImage cloudImage, @NotNull CloudInstanceUserData cloudInstanceUserData) throws QuotaException {
        return null;
    }

    @Override
    public void restartInstance(@NotNull CloudInstance cloudInstance) {

    }

    @Override
    public void terminateInstance(@NotNull CloudInstance cloudInstance) {

    }

    @Override
    public void dispose() {

    }

    @Override
    public boolean isInitialized() {
        return false;
    }

    @Nullable
    @Override
    public CloudImage findImageById(@NotNull String s) throws CloudException {
        return null;
    }

    @Nullable
    @Override
    public CloudInstance findInstanceByAgent(@NotNull AgentDescription agentDescription) {
        return null;
    }

    @NotNull
    @Override
    public Collection<? extends CloudImage> getImages() throws CloudException {
        return null;
    }

    @Nullable
    @Override
    public CloudErrorInfo getErrorInfo() {
        return null;
    }

    @Override
    public boolean canStartNewInstance(@NotNull CloudImage cloudImage) {
        return false;
    }

    @Nullable
    @Override
    public String generateAgentName(@NotNull AgentDescription agentDescription) {
        return null;
    }
}
