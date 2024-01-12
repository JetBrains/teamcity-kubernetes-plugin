
package jetbrains.buildServer.clouds.kubernetes.auth

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.Pair
import io.fabric8.kubernetes.client.ConfigBuilder
import jetbrains.buildServer.Used
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnection
import jetbrains.buildServer.util.TimeService
import java.util.concurrent.ConcurrentHashMap

abstract class RefreshableStrategy<T>(private val myTimeService: TimeService) : KubeAuthStrategy {
    companion object {
        val CACHED_TOKENS = ConcurrentHashMap<Pair<String, String>, Pair<String, Long>>()
        val LOG = Logger.getInstance(this::class.qualifiedName)

        @Used("Tests")
        @Deprecated("")
        fun invalidateAll() {
            CACHED_TOKENS.clear()
        }
    }

    override fun apply(clientConfig: ConfigBuilder, connection: KubeApiConnection): ConfigBuilder {
        val dataHolder = createData(connection)
        val token = createToken(dataHolder)

        return clientConfig.withOauthToken(token);
    }

    override fun isRefreshable() = true

    override fun invalidate(connection: KubeApiConnection) {
        invalidateToken(createKey(createData(connection)), true)
    }


    private fun createToken(dataHolder: T): String? {
        val currentToken = getOrExpiry(dataHolder)
        if (currentToken != null)
            return currentToken

        val newTokenPair = retrieveNewToken(dataHolder) ?: return null

        cache(dataHolder, newTokenPair.first, newTokenPair.second)
        return newTokenPair.getFirst()
    }

    private fun getOrExpiry(dataHolder: T): String? {
        val key = createKey(dataHolder)
        val token = CACHED_TOKENS[key] ?: return null

        val expireTime = token.getSecond()
        if (myTimeService.now() >= expireTime) {
            invalidateToken(key, false)
        } else {
            return token.getFirst()
        }

        return null
    }

    private fun invalidateToken(key: Pair<String, String>, forceInvalidate: Boolean) {
        val force = if (forceInvalidate) "Force " else ""
        LOG.info("${force}invalidating token for ${key.first}:${key.second}")
        synchronized(CACHED_TOKENS) {
            val token2 = CACHED_TOKENS[key]
            if (forceInvalidate || token2 != null && myTimeService.now() >= token2.getSecond()) {
                CACHED_TOKENS.remove(key)
            }
        }
    }

    private fun cache(dataHolder: T, idToken: String, expireTimeSec: Long) {
        val key = createKey(dataHolder)
        synchronized(CACHED_TOKENS) {
            CACHED_TOKENS.put(key, Pair.create(idToken, myTimeService.now() + expireTimeSec * 1000L))
        }
    }

    protected abstract fun retrieveNewToken(dataHolder: T) : Pair<String, Long>?

    protected abstract fun createKey(dataHolder: T) : Pair<String, String>

    protected abstract fun createData(connection: KubeApiConnection) : T

}