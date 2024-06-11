package sapala.s2sauthservice.entity

import io.swagger.v3.oas.annotations.media.Schema
import java.net.URL

data class SynchronizePublicKeys(
    @Schema(example = "https://localhost:8079/s2s-auth-service/api/v1/public-keys")
    val publicKeysUrl: URL
)
