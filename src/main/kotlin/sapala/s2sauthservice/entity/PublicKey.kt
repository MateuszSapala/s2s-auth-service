package sapala.s2sauthservice.entity

import io.swagger.v3.oas.annotations.media.Schema

data class PublicKey(
    @Schema(example = "RS256")
    val type: String,
    @Schema(example = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA3q3wNnZoCXak5aAi2B8Fa3NnVfNam/MqplrCs5gfe+gcEtIJZWwsq2uNBY+zn8evOWYk0Nt7RV+vU5+ML9qELUZ9j+gwBsvz6DFc8JTj5SmfFstdVqFNUGwQBhxUTDiOKzDAc4e/iXEN/PZW4gSXZVDiTWyKBoGvvv60K74+dMXHMFhoR2cbe2kSnf0rbAoQ+IX7ketj9PmomaJyJecQZNutZ9TvXWMUPu6yfllc2cFSI1Ecu+mbkFOLR8qVRaRAPjbcevgAuau1EUCh5zbOLoPWfUHSrwLetU+UeJlWs3SfoJ2BZu3/06aeOLEUAyNEZqYIxUF0aawIPDV3g4ZmqQIDAQAB")
    val publicKey: String,
    @Schema(example = "localhost")
    val generatedBy: String,
    @Schema(example = "1717429061735")
    val generatedOn: Long
)
