package sapala.s2sauthservice.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import sapala.s2sauthservice.config.Env
import sapala.s2sauthservice.config.ServicesConfig
import sapala.s2sauthservice.entity.PublicKey
import sapala.s2sauthservice.entity.SynchronizePublicKeys
import java.net.URL

@Service
class TokenSynchronizationService(
    private val env: Env,
    private val servicesConfig: ServicesConfig,
    private val client: OkHttpClient,
    private val mapper: ObjectMapper
) {
    companion object {
        private val log = LoggerFactory.getLogger(TokenSynchronizationService::class.java)
        private val JSON: MediaType = "application/json; charset=utf-8".toMediaType()
        private fun URL.toPublicKeysUrl() = this.toURI().resolve("/s2s-auth-service/api/v1/public-keys").toURL()
        private fun URL.toSynchronizeKeysUrl() =
            this.toURI().resolve("/s2s-auth-service/api/v1/synchronize-keys").toURL()
    }

    private fun URL.isThisInstance() = this == env.serverUrl()

    fun getPublicKeysFromOtherInstances(): MutableMap<String, PublicKey> {
        val publicKeys = mutableMapOf<String, PublicKey>()
        for (url in servicesConfig.s2sAuthServiceUrls()) {
            if (url.isThisInstance()) {
                continue
            }
            val keys = getPublicKeysFromOtherInstance(url.toPublicKeysUrl()) ?: continue
            publicKeys.putAll(keys)
        }
        return publicKeys
    }

    fun getPublicKeysFromOtherInstance(publicKeysUrl: URL): Map<String, PublicKey>? {
        val request = Request.Builder()
            .url(publicKeysUrl)
            .get()
            .build()
        val call = client.newCall(request)

        log.info("Getting public keys from {}", publicKeysUrl)
        try {
            val response = call.execute()
            if (!response.isSuccessful) {
                log.warn(
                    "Unable to get public keys from {}. Response {}: '{}'",
                    publicKeysUrl,
                    response.code,
                    response.body?.string()
                )
                return null
            }
            return mapper.readValue<Map<String, PublicKey>>(response.body!!.string())
        } catch (ex: Exception) {
            log.error("Unable to get public keys from {}. {}", publicKeysUrl, ex.message)
            return null
        }
    }

    fun requestKeySynchronizationOnOtherInstances() {
        val synchronizePublicKeys = SynchronizePublicKeys(env.serverUrl().toPublicKeysUrl())
        for (url in servicesConfig.s2sAuthServiceUrls()) {
            if (url.isThisInstance()) {
                continue
            }
            requestKeySynchronizationOnOtherInstance(url.toSynchronizeKeysUrl(), synchronizePublicKeys) ?: continue
        }
    }

    private fun requestKeySynchronizationOnOtherInstance(
        synchronizeKeysUrl: URL,
        synchronizePublicKeys: SynchronizePublicKeys
    ) {
        val body = mapper.writeValueAsString(synchronizePublicKeys).toRequestBody(JSON)
        val request = Request.Builder()
            .url(synchronizeKeysUrl)
            .post(body)
            .build()
        val call = client.newCall(request)

        log.info("Requesting synchronization of public keys on {}", synchronizeKeysUrl)
        try {
            val response = call.execute()
            if (!response.isSuccessful) {
                log.warn(
                    "Unable to request public keys synchronization on {}. Response {}: '{}'",
                    synchronizeKeysUrl,
                    response.code,
                    response.body?.string()
                )
                return
            }
        } catch (ex: Exception) {
            log.error(
                "Unable to request public keys synchronization on {}. {}",
                synchronizeKeysUrl,
                ex.message
            )
        }
    }
}