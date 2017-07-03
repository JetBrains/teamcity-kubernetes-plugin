package jetbrains.buildServer.clouds.kubernetes.auth;

import io.fabric8.kubernetes.client.ConfigBuilder;
import jetbrains.buildServer.clouds.kubernetes.KubeCloudException;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnection;
import jetbrains.buildServer.util.StringUtil;
import org.apache.commons.codec.binary.Base64;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants.CLIENT_CERTIFICATE_DATA;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 26.06.17.
 */
public class ClientCertificateAuthStrategy implements KubeAuthStrategy {
    @NotNull
    @Override
    public String getId() {
        return "client-cert";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Client Certificate";
    }

    @Nullable
    @Override
    public String getDescription() {
        return "Authenticate with X509 Client Certificate";
    }

    @NotNull
    @Override
    public ConfigBuilder apply(@NotNull ConfigBuilder clientConfig, @NotNull KubeApiConnection connection) {
        String clientCertData = connection.getCustomParameter(CLIENT_CERTIFICATE_DATA);
        if(StringUtil.isEmpty(clientCertData)) {
            throw new KubeCloudException("Client certificate data is empty for connection " + connection);
        }
        return clientConfig.withClientCertData(Base64.encodeBase64String(clientCertData.getBytes()));
    }
}
