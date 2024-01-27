package sapala.s2sauthservice.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

@Configuration
class Config(val env: Environment) {
    @Bean
    fun springShopOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info().title("s2s-auth-service")
                    .description("Service for service-to-service authentication")
                    .version(env.getProperty("build.version"))
            )
    }
}
