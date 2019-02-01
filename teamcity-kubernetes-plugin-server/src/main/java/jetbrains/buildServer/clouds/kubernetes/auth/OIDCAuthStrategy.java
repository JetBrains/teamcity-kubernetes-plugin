package jetbrains.buildServer.clouds.kubernetes.auth;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.StreamUtil;
import io.fabric8.kubernetes.client.ConfigBuilder;
import java.io.InputStream;
import java.net.*;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import jetbrains.buildServer.clouds.kubernetes.KubeCloudException;
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnection;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.util.TimeService;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.crypto.codec.Base64;

import static jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants.*;

public class OIDCAuthStrategy implements KubeAuthStrategy {

  private TimeService myTimeService;

  public OIDCAuthStrategy(@NotNull TimeService timeService) {
    myTimeService = timeService;
  }

  private static final ConcurrentMap<Pair<String, String>, Pair<String, Long>> CACHED_TOKENS = new ConcurrentHashMap<>();


  @NotNull
  @Override
  public String getId() {
    return "oidc";
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return "Open ID";
  }

  @Nullable
  @Override
  public String getDescription() {
    return "Authenticate with Open ID provider";
  }

  @NotNull
  @Override
  public ConfigBuilder apply(@NotNull final ConfigBuilder clientConfig, @NotNull final KubeApiConnection connection) {

    final DataHolder dataHolder = DataHolder.createFromConnection(connection);

    final String token = createToken(dataHolder);
    return clientConfig.withOauthToken(token);
  }

  @Override
  public boolean isRefreshable() {
    return true;
  }

  @Override
  public void invalidate(final KubeApiConnection connection) {
    final DataHolder dataHolder = DataHolder.createFromConnection(connection);
    invalidateToken(dataHolder.myClientId, dataHolder.myIssuerUrl, true);
  }

  private String createToken(final DataHolder dataHolder){
    final String currentToken = getOrExpiry(dataHolder.myClientId, dataHolder.myIssuerUrl);
    if (currentToken != null)
      return currentToken;

    final Pair<String, Long> newTokenPair = retrieveNewToken(dataHolder);
    if (newTokenPair == null)
      return null;

    cache(dataHolder.myClientId, dataHolder.myIssuerUrl, newTokenPair.first, newTokenPair.second);
    return newTokenPair.getFirst();
  }

  @Nullable
  protected Pair<String, Long> retrieveNewToken(@NotNull final DataHolder dataHolder) {
    InputStream stream = null;
    try {
      URL providerConfigurationURL = new URI(dataHolder.myIssuerUrl).resolve("/.well-known/openid-configuration").toURL();
      stream = providerConfigurationURL.openStream();

      final String text = StreamUtil.readText(stream);
      System.out.println();
      JsonParser parser = new JsonParser();
      final JsonElement element = parser.parse(text);
      final String tokenEndpoint = element.getAsJsonObject().get("token_endpoint").getAsString();
      final HttpClientBuilder clientBuilder = HttpClientBuilder.create();
      final CloseableHttpClient build = clientBuilder.build();

      final HttpPost request = new HttpPost(tokenEndpoint);
      request.setEntity(new UrlEncodedFormEntity(Arrays.asList(new BasicNameValuePair("refresh_token", dataHolder.myRefreshToken),
                                                               new BasicNameValuePair("grant_type", "refresh_token"))));

      request.setHeader("Authorization", "Basic " + new String(Base64.encode((dataHolder.myClientId + ":" + dataHolder.myClientSecret).getBytes())));
      final CloseableHttpResponse response = build.execute(request);
      if (response.getStatusLine()!= null && response.getStatusLine().getStatusCode() == 200){
        final String tokenData = StreamUtil.readText(response.getEntity().getContent());
        final JsonElement tokenRequestElement = parser.parse(tokenData);
        final JsonObject tokenRequestObj = tokenRequestElement.getAsJsonObject();
        final String idToken = tokenRequestObj.get("id_token").getAsString();
        final long expireMs;
        if(tokenRequestObj.has("expires_in")) {
          expireMs = tokenRequestObj.get("expires_in").getAsLong();
        } else {
          expireMs = 365*24*86400l*1000l; //one year
        }
        return Pair.create(idToken, expireMs);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      FileUtil.close(stream);
    }
    return null;
  }

  @Nullable
  private String getOrExpiry(final String clientId, final String issuerUrl){
    final Pair<String, String> key = Pair.create(clientId, issuerUrl);
    final Pair<String, Long> token = CACHED_TOKENS.get(key);
    if (token == null)
      return null;

    final long expireTime = token.getSecond();
    if (myTimeService.now() >= expireTime){
      invalidateToken(clientId, issuerUrl, false);
    } else {
      return token.getFirst();
    }

    return null;
  }

  private void invalidateToken(@NotNull final String clientId, @NotNull final String issuerUrl, boolean forceInvalidate){
    final Pair<String, String> key = Pair.create(clientId, issuerUrl);
    synchronized (CACHED_TOKENS){
      final Pair<String, Long> token2 = CACHED_TOKENS.get(key);
      if (forceInvalidate || (token2 != null && myTimeService.now() >= token2.getSecond())){
        CACHED_TOKENS.remove(key);
      }
    }
  }

  private void cache(final String clientId, final String issuerUrl, final String idToken, final long expireTimeSec){
    final Pair<String, String> key = Pair.create(clientId, issuerUrl);
    synchronized (CACHED_TOKENS){
      CACHED_TOKENS.put(key, Pair.create(idToken, myTimeService.now() + expireTimeSec * 1000l));
    }
  }

  protected static class DataHolder {
    private final String myClientId;
    private final String myClientSecret;
    private final String myIssuerUrl;
    private final String myRefreshToken;

    private DataHolder(final String clientId, final String clientSecret, final String issuerUrl, final String refreshToken) {
      myClientId = clientId;
      myClientSecret = clientSecret;
      myIssuerUrl = issuerUrl;
      myRefreshToken = refreshToken;
    }

    public static DataHolder createFromConnection(@NotNull final KubeApiConnection connection){
      final String clientId = connection.getCustomParameter(OIDC_CLIENT_ID);
      if(StringUtil.isEmpty(clientId)) {
        throw new KubeCloudException("Client ID is empty for connection to " + connection.getApiServerUrl());
      }

      final String clientSecret = connection.getCustomParameter(OIDC_CLIENT_SECRET);
      if(StringUtil.isEmpty(clientSecret)) {
        throw new KubeCloudException("Client secret is empty for connection to " + connection.getApiServerUrl());
      }

      final String issuerUrl = connection.getCustomParameter(OIDC_ISSUER_URL);
      if(StringUtil.isEmpty(issuerUrl)) {
        throw new KubeCloudException("Issuer URL is empty for connection to " + connection.getApiServerUrl());
      }

      final String refreshToken = connection.getCustomParameter(OIDC_REFRESH_TOKEN);
      if(StringUtil.isEmpty(refreshToken)) {
        throw new KubeCloudException("Refresh token is empty for connection to " + connection.getApiServerUrl());
      }
      return new DataHolder(clientId, clientSecret, issuerUrl, refreshToken);
    }
  }
}
