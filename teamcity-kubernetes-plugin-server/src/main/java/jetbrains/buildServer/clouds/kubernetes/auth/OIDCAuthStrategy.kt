
package jetbrains.buildServer.clouds.kubernetes.auth

import com.google.gson.JsonParser
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.io.StreamUtil
import jetbrains.buildServer.clouds.kubernetes.KubeCloudException
import jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants.*
import jetbrains.buildServer.clouds.kubernetes.auth.KubeAuthStrategy.*
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnection
import jetbrains.buildServer.serverSide.InvalidProperty
import jetbrains.buildServer.util.FileUtil
import jetbrains.buildServer.util.TimeService
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicNameValuePair
import java.io.InputStream
import java.net.URI
import java.util.*

private fun String.ensureTrailingSlash(): String = if (this.endsWith("/")) this else "$this/"

class OIDCAuthStrategy(myTimeService: TimeService) : RefreshableStrategy<OIDCData>(myTimeService) {

    override fun retrieveNewToken(dataHolder: OIDCData): Pair<String, Long>? {
        val authEndpoint = ".well-known/openid-configuration"
        val tokenV2 = doRetrieveToken(dataHolder, "v2.0/$authEndpoint")
        return if (tokenV2 != null){
            tokenV2
        } else {
            doRetrieveToken(dataHolder, authEndpoint)
        }
    }

    private fun doRetrieveToken(dataHolder: OIDCData, authorizationEndpoint: String): Pair<String, Long>? {
        var stream: InputStream? = null
        try {
            val providerConfigurationURL = URI(dataHolder.myIssuerUrl).resolve(authorizationEndpoint).toURL()
            stream = providerConfigurationURL.openStream()

            val text = StreamUtil.readText(stream!!)
            println()
            val parser = JsonParser()
            val element = parser.parse(text)
            val tokenEndpoint = element.asJsonObject.get("token_endpoint").asString
            val clientBuilder = HttpClientBuilder.create()
            val build = clientBuilder.build()

            val request = HttpPost(tokenEndpoint)
            request.entity = UrlEncodedFormEntity(
                Arrays.asList(
                    BasicNameValuePair("refresh_token", dataHolder.myRefreshToken),
                    BasicNameValuePair("grant_type", "refresh_token")
                )
            )

            request.setHeader("Authorization", "Basic " + String(Base64.getEncoder().encode((dataHolder.myClientId + ":" + dataHolder.myClientSecret).toByteArray())))
            val response = build.execute(request)
            if (response.statusLine != null && response.statusLine.statusCode == 200) {
                val tokenData = StreamUtil.readText(response.entity.content)
                val tokenRequestElement = parser.parse(tokenData)
                val tokenRequestObj = tokenRequestElement.asJsonObject
                val idToken = tokenRequestObj.get("id_token").asString
                val expireSec: Long
                if (tokenRequestObj.has("expires_in")) {
                    expireSec = tokenRequestObj.get("expires_in").asLong
                } else {
                    expireSec = 365 * 24 * 86400L //one year
                }
                LOG.info("Retrieved new token for user ${dataHolder.myClientId} from ${dataHolder.myIssuerUrl}. Token expires in ${expireSec} sec")
                return Pair.create(idToken, expireSec)
            }
        } catch (e: Exception) {
            LOG.warnAndDebugDetails("An error occurred while retrieving token for user ${dataHolder.myClientId} from ${dataHolder.myIssuerUrl}", e)
        } finally {
            FileUtil.close(stream)
        }
        return null
    }

    override fun createKey(dataHolder: OIDCData): Pair<String, String> {
        return Pair.create(dataHolder.myClientId, dataHolder.myIssuerUrl)
    }

    override fun createData(connection: KubeApiConnection): OIDCData {
        val clientId = connection.getCustomParameter(OIDC_CLIENT_ID) ?: throw KubeCloudException("Client ID is empty for connection to " + connection.apiServerUrl)
        val clientSecret = connection.getCustomParameter(SECURE_PREFIX + OIDC_CLIENT_SECRET) ?: throw KubeCloudException("Client secret is empty for connection to " + connection.apiServerUrl)
        val issuerUrl = connection.getCustomParameter(OIDC_ISSUER_URL) ?: throw KubeCloudException("Issuer URL is empty for connection to " + connection.apiServerUrl)
        val refreshToken = connection.getCustomParameter(SECURE_PREFIX + OIDC_REFRESH_TOKEN) ?: throw KubeCloudException("Refresh token is empty for connection to " + connection.apiServerUrl)

        return OIDCData(clientId, clientSecret, issuerUrl.ensureTrailingSlash(), refreshToken)
    }

    override fun getId() = "oidc"

    override fun getDisplayName() = "Open ID"

    override fun getDescription() = "Authenticate with Open ID provider"
    override fun process(props: MutableMap<String, String>): MutableCollection<InvalidProperty> {
        val retval = arrayListOf<InvalidProperty>()
        if (props[OIDC_CLIENT_ID].isNullOrEmpty()) {
            retval.add(InvalidProperty(OIDC_CLIENT_ID, "Client ID is required"))
        }
        if (props[SECURE_PREFIX + OIDC_CLIENT_SECRET].isNullOrEmpty()) {
            retval.add(InvalidProperty(OIDC_CLIENT_SECRET, "Client secret is required"))
        }
        if (props[OIDC_ISSUER_URL].isNullOrEmpty()) {
            retval.add(InvalidProperty(OIDC_ISSUER_URL, "Client ID is required"))
        }
        if (props[SECURE_PREFIX + OIDC_REFRESH_TOKEN].isNullOrEmpty()) {
            retval.add(InvalidProperty(OIDC_REFRESH_TOKEN, "Client ID is required"))
        }
        return retval
    }
}

data class OIDCData(val myClientId: String,
                    val myClientSecret: String,
                    val myIssuerUrl: String,
                    val myRefreshToken: String)