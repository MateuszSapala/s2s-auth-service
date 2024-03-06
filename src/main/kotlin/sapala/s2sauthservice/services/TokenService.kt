package sapala.s2sauthservice.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Encoders
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
import sapala.s2sauthservice.entity.SendToken
import sapala.s2sauthservice.entity.SynchronizePublicKeys
import java.net.URL
import java.time.Instant
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess


@Service
class TokenService(
    private val env: Env,
    private val servicesConfig: ServicesConfig,
    private val synchronizationService: TokenSynchronizationService,
    private val client: OkHttpClient,
    private val mapper: ObjectMapper
) {
    companion object {
        private val log = LoggerFactory.getLogger(TokenService::class.java)
        private val JSON: MediaType = "application/json; charset=utf-8".toMediaType()
    }

    private val executor = Executors.newVirtualThreadPerTaskExecutor()
    private val keyId = UUID.randomUUID().toString()
    private val algorithm = Jwts.SIG.EdDSA
    private val keyPair = algorithm.keyPair().build()
    val publicKeys = initializePublicKeys()

    private fun initializePublicKeys(): MutableMap<String, PublicKey> {
        val file = env.publicKeyFile()
        val publicKeys = synchronizationService.getPublicKeysFromOtherInstances()
        if (publicKeys.keys.isEmpty()) {
            if (!env.independentStart()) {
                log.error("The application cannot be started because it failed to retrieve public keys from other instances and the 'independent-start' property was not set to true")
                exitProcess(10)
            }
            if (file.exists()) {
                val keys = mapper.readValue<MutableMap<String, PublicKey>>(file.readText())
                publicKeys.putAll(keys)
            }
        }
        val encodedPublicKey = Encoders.BASE64.encode(keyPair.public.encoded)
        publicKeys[keyId] = PublicKey(algorithm.id, encodedPublicKey, env.host(), System.currentTimeMillis())
        file.parentFile.mkdirs()
        file.writeText(mapper.writeValueAsString(publicKeys))
        scheduleExpiredPublicKeysRemoval()
        return publicKeys
    }

    private fun scheduleExpiredPublicKeysRemoval() {
        Executors.newSingleThreadScheduledExecutor()
            .scheduleAtFixedRate(this::removeExpiredPublicKeys, 1, 60, TimeUnit.MINUTES)
    }

    private fun removeExpiredPublicKeys() {
        val tokenValidity = env.tokenExpirationTime().toMillis()
        val groupedKeys = publicKeys.entries.groupBy { it.value.generatedBy }
        var anyChange = false
        for ((_, map) in groupedKeys) {
            val latestKeyGeneratedOn = map.map { it.value }.maxOf { it.generatedOn }
            val expiredKeyIds = map.filter { it.value.generatedOn < latestKeyGeneratedOn - tokenValidity }
                .map { it.key }
            for (keyId in expiredKeyIds) {
                publicKeys.remove(keyId)
                anyChange = true
            }
        }
        if (anyChange) {
            env.publicKeyFile().writeText(mapper.writeValueAsString(publicKeys))
        }
    }


    fun requestToken(serviceName: String, tokenReceiverUrl: URL) {
        executor.execute {
            if (!servicesConfig.isCorrectReceiverUrl(serviceName, tokenReceiverUrl)) {
                return@execute
            }
            val token = generateToken(serviceName)
            sendToken(SendToken(token), tokenReceiverUrl)
        }
    }

    private fun Instant.getExpirationEpochMilli() = this.plusMillis(env.tokenExpirationTime().toMillis()).toEpochMilli()

    fun generateToken(serviceName: String): String {
        val now = Instant.now()
        return Jwts.builder()
            .header()
            .keyId(keyId)
            .and()
            .claim("serviceName", serviceName)
            .signWith(keyPair.private)
            .issuedAt(Date(now.toEpochMilli()))
            .expiration(Date(now.getExpirationEpochMilli()))
            .compact()
    }

    fun sendToken(token: SendToken, url: URL) {
        val body = mapper.writeValueAsString(token).toRequestBody(JSON)
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer " + generateToken("s2s-auth-service"))
            .post(body)
            .build()
        val call = client.newCall(request)

        val response = call.execute()
        if (!response.isSuccessful) {
            log.warn("Unable to send token to {}. Response {}: '{}'", url, response.code, response.body?.string())
            return
        }
        log.info("Successfully send token to {}", url)
    }

    fun synchronizePublicKeys(publicKeysUrl: URL) {
        executor.execute {
            if (!servicesConfig.isCorrectReceiverUrl("s2s-auth-service", publicKeysUrl)) {
                return@execute
            }
            val keys = synchronizationService.getPublicKeysFromOtherInstance(publicKeysUrl) ?: return@execute
            publicKeys.putAll(keys)
        }
    }

}
