
package jetbrains.buildServer.clouds.kubernetes.auth;

import io.fabric8.kubernetes.client.ConfigBuilder;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;
import jetbrains.buildServer.clouds.kubernetes.KubeCloudException;
import jetbrains.buildServer.clouds.kubernetes.KubeUtils;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnection;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.util.StringUtil;
import org.apache.commons.codec.binary.Base64;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants.CLIENT_CERTIFICATE_DATA;
import static jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants.CLIENT_KEY_DATA;

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
        return "Client certificate & key";
    }

    @Nullable
    @Override
    public String getDescription() {
        return "Authenticate with the client certificate & the key";
    }

    @NotNull
    @Override
    public ConfigBuilder apply(@NotNull ConfigBuilder clientConfig, @NotNull KubeApiConnection connection) {
        String clientCertData = connection.getCustomParameter(SECURE_PREFIX+ CLIENT_CERTIFICATE_DATA);
        String clientKeyData = connection.getCustomParameter(SECURE_PREFIX+ CLIENT_KEY_DATA);
        if(StringUtil.isEmpty(clientCertData)) {
            throw new KubeCloudException("Client certificate data is empty");
        }
        if(StringUtil.isEmpty(clientKeyData)) {
            throw new KubeCloudException("Client key data is empty");
        }
        return clientConfig.withClientCertData(KubeUtils.encodeBase64IfNecessary(clientCertData))
                           .withClientKeyData(KubeUtils.encodeBase64IfNecessary(clientKeyData));
    }

    @Override
    public Collection<InvalidProperty> process(final Map<String, String> properties) {
        List<InvalidProperty> retval = new ArrayList<>();
        String certData = properties.get(SECURE_PREFIX+ CLIENT_CERTIFICATE_DATA);
        if (StringUtil.isEmpty(certData)){
            retval.add(new InvalidProperty(CLIENT_CERTIFICATE_DATA, "Client certificate data is empty"));
        }
        String keyData = properties.get(SECURE_PREFIX + CLIENT_KEY_DATA);
        if (StringUtil.isEmpty(keyData)){
            retval.add(new InvalidProperty(CLIENT_KEY_DATA, "Client key data is empty"));
        }
        return retval;
    }
}