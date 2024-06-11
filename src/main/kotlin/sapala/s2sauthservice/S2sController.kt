package sapala.s2sauthservice

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import sapala.s2sauthservice.entity.PublicKey
import sapala.s2sauthservice.entity.RequestToken
import sapala.s2sauthservice.entity.SynchronizePublicKeys
import sapala.s2sauthservice.services.TokenService

@RestController
@RequestMapping("/s2s-auth-service/api/v1")
class S2sController(private val tokenService: TokenService) {
    companion object {
        private val log = LoggerFactory.getLogger(S2sController::class.java)
    }

    @PostMapping("/request-token")
    @Operation(summary = "Endpoint to request an s2s token, which will be sent in a separate request to the URL specified in the query body")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @ApiResponses(value = [ApiResponse(responseCode = "204", description = "No Content")])
    fun requestToken(@RequestBody body: RequestToken) {
        log.info("Service '${body.serviceName}' requested to receive token on '${body.tokenReceiverUrl}'")
        tokenService.requestToken(body.serviceName, body.tokenReceiverUrl)
    }

    @GetMapping("/public-keys")
    @Operation(
        summary = "Endpoint to retrieve public keys that can be used to verify the token", responses = [
            ApiResponse(
                responseCode = "200", content = [Content(
                    mediaType = "application/json",
                    examples = [ExampleObject(
                        value = "{\"eae4d40d-51e7-4ba2-8fc2-de8d95477b93\":{\"type\":\"RS256\",\"publicKey\":\"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA3q3wNnZoCXak5aAi2B8Fa3NnVfNam/MqplrCs5gfe+gcEtIJZWwsq2uNBY+zn8evOWYk0Nt7RV+vU5+ML9qELUZ9j+gwBsvz6DFc8JTj5SmfFstdVqFNUGwQBhxUTDiOKzDAc4e/iXEN/PZW4gSXZVDiTWyKBoGvvv60K74+dMXHMFhoR2cbe2kSnf0rbAoQ+IX7ketj9PmomaJyJecQZNutZ9TvXWMUPu6yfllc2cFSI1Ecu+mbkFOLR8qVRaRAPjbcevgAuau1EUCh5zbOLoPWfUHSrwLetU+UeJlWs3SfoJ2BZu3/06aeOLEUAyNEZqYIxUF0aawIPDV3g4ZmqQIDAQAB\",\"generatedBy\":\"localhost\",\"generatedOn\":1717429061735},\"fb09c1c3-7399-4b43-b15e-c5de239a33ff\":{\"type\":\"RS256\",\"publicKey\":\"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA1QPNUOi7exBGDw64cPsvA75Q/utSKcpkG8yz3hq04PqT8lvfYt2VncyQBKwE8Ynf0jVlC1RZ6YFx8u1JkCLoGJcimtBe6f7iMaqEUHWCMQ8AxMepFNDVlhbRVxQqD5gQbjuxwfMPLCYyKpsvvkl8kD1S5Tcom6tdHCC65//gAtmY71W4ywU0cR3OnF08xgx5TrDLNHZXcPoBSrZ5ewnV3gcVOBwxhAe0YN3ZChFHVyla7qomJqZq8SDoUC+D5D6jUqkWKZcXWOad1LCVTijXx5UaEmAhiGO6nIAiN5hvG6rUCS/ojUNJxxjJWfzD4fmhi/tE1gVL3X5eL5v/6Wtt3QIDAQAB\",\"generatedBy\":\"localhost\",\"generatedOn\":1717429493982},\"f2178e43-e4b2-4215-a16d-dcf041996910\":{\"type\":\"RS256\",\"publicKey\":\"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqOf0CJ72TG8Yo7UaiOqywfZ82AF88F5HlC/lJeAOeNCkkZ4Nb+EOmts5XxYYrHNHuIDRyquVm5uTe9mQ4e4I++IwPZuiYxJC58PNurr8em/iO09ARY3qXAsizn2K86YF0zOULqVR3Y/K3RqubPzUNqhV+A3gdfteslndP5sl3adJuNezx7M9iYhfYDW7wOSGdct2sIKvPWO/urFIlj5zWbXsqWQ30qsjWraDQTXVS8PJA/lzfKNj+TYbnC6l6AG3wjGEMmZS7SGRmyY8llvfXtywSDR3iwYNhRpm3W804ee/TxRVShZH2OjhffzzkTRddmAAWOxCfLu8Bggv7kxbQwIDAQAB\",\"generatedBy\":\"localhost\",\"generatedOn\":1717429834991}}"
                    )]
                )]
            )
        ]
    )
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "OK")])
    fun getPublicKeys(): Map<String, PublicKey> {
        return tokenService.publicKeys
    }

    @PostMapping("/synchronize-keys")
    @Operation(summary = "After calling the endpoint, the application retrieves public keys from another instance of the s2s-auth-service in order to synchronize public keys between them")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @ApiResponses(value = [ApiResponse(responseCode = "204", description = "No Content")])
    fun synchronizeKeys(@RequestBody body: SynchronizePublicKeys) {
        tokenService.synchronizePublicKeys(body.publicKeysUrl)
    }
}
