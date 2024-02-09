package sapala.s2sauthservice

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Encoders
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import sapala.s2sauthservice.config.Env
import sapala.s2sauthservice.config.ServicesConfig
import sapala.s2sauthservice.entity.PublicKey
import sapala.s2sauthservice.entity.SendToken
import java.net.URL
import java.time.Instant
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


@Service
class TokenService(
    private val env: Env,
    private val servicesConfig: ServicesConfig,
    private val client: OkHttpClient,
    private val mapper: ObjectMapper
) {
    companion object {
        private val log = LoggerFactory.getLogger(TokenService::class.java)
    }

    private val executor = Executors.newVirtualThreadPerTaskExecutor()
    private val keyId = UUID.randomUUID().toString()
    private val algorithm = Jwts.SIG.EdDSA
    private val keyPair = algorithm.keyPair().build()
    private val publicKeys = initializePublicKeys()

    private fun initializePublicKeys(): MutableMap<String, PublicKey> {
        val file = env.publicKeyFile()
        val publicKeys =
            if (file.exists()) mapper.readValue<MutableMap<String, PublicKey>>(file.readText()) else HashMap()
        val encodedPublicKey = Encoders.BASE64.encode(keyPair.public.encoded)
        publicKeys[keyId] = PublicKey(algorithm.id, encodedPublicKey, env.host(), System.currentTimeMillis())
        file.parentFile.mkdirs()
        file.writeText(mapper.writeValueAsString(publicKeys))
        scheduleExpiredPublicKeysRemoval()
        return publicKeys
    }

    private fun scheduleExpiredPublicKeysRemoval() {
        Executors.newSingleThreadScheduledExecutor()
            .scheduleAtFixedRate(this::removeExpiredPublicKeys, 0, 1, TimeUnit.HOURS)
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

    fun getExpirationEpochMilli() = Instant.now().plusMillis(env.tokenExpirationTime().toMillis()).toEpochMilli()

    fun generateToken(serviceName: String): String {
        return Jwts.builder()
            .header()
            .keyId(keyId)
            .and()
            .claim("serviceName", serviceName)
            .signWith(keyPair.private)
            .expiration(Date(getExpirationEpochMilli()))
            .compact()
    }

    fun sendToken(token: SendToken, url: URL) {
        val body = mapper.writeValueAsString(token).toRequestBody()
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer " + generateToken("s2s-auth-service"))
            .post(body)
            .build()
        val call = client.newCall(request)

        val response = call.execute()
        if (!response.isSuccessful) {
            log.warn("Unable to send token to {}", url)
            return
        }
        log.info("Successfully send token to {}", url)
    }

    fun getPublicKeys(): Map<String, PublicKey> {
        return publicKeys
    }
}
