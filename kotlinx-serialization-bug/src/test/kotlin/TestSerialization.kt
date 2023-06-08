import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerCN
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind.STRING
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.UUID

object UUIDAdapter : KSerializer<UUID> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UUID", STRING)

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): UUID {
        return UUID.fromString(decoder.decodeString())
    }
}

class TestSerialization {
    @Test
    fun `test serialization bug with contextual adapters for arrays`() = testApplication {
        val testHttpClient = createClient {
            install(ContentNegotiation) {
                json(
                    Json {
                        serializersModule = SerializersModule {
                            contextual(UUID::class, UUIDAdapter)
                        }
                    }
                )
            }
            defaultRequest {
                contentType(ContentType.Application.Json)
            }
        }
        application {
            install(ServerCN) {
                json(
                    Json {
                        serializersModule = SerializersModule {
                            contextual(UUID::class, UUIDAdapter)
                        }
                    }
                )
            }
            routing {
                post("/echoUUIDs") {
                    val bytes = call.receive<List<UUID>>()
                    call.respond(bytes.toString())
                }
            }
        }

        val response = testHttpClient.post("/echoUUIDs") {
            setBody(listOf(UUID.randomUUID(), UUID.randomUUID()))
        }

        val result = response.body<String>()
        Assertions.assertTrue(result.isNotEmpty())
    }
}