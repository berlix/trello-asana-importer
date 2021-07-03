package pro.felixo.importer

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.net.URL
import java.time.Instant

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = Instant::class)
object UrlSerializer : KSerializer<URL> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("pro.felixo.importer.UrlSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: URL) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): URL = URL(decoder.decodeString())
}
