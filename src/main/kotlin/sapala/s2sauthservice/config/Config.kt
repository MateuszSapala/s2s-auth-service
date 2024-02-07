package sapala.s2sauthservice.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import okhttp3.OkHttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class Config(val env: Env) {
    @Bean
    fun springShopOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info().title("s2s-auth-service")
                    .description("Service for service-to-service authentication")
                    .version(env.version())
            )
    }

    @Bean
    fun okHttpClient(): OkHttpClient {
        return OkHttpClient()
    }
}
