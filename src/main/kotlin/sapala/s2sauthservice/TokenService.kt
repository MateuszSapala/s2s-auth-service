package sapala.s2sauthservice

import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.Jwts
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import sapala.s2sauthservice.config.Env
import sapala.s2sauthservice.config.ServicesConfig
import sapala.s2sauthservice.entity.SendToken
import java.net.URL
import java.time.Instant
import java.util.*
import java.util.concurrent.Executors


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
    private val uuid = UUID.randomUUID().toString()
    private val algorithm = Jwts.SIG.EdDSA
    private val keyPair = algorithm.keyPair().build()

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
            .keyId(uuid)
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
}
