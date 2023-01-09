package social.plasma.relay

import com.squareup.moshi.*
import okio.ByteString
import okio.ByteString.Companion.decodeHex
import social.plasma.relay.Filters
import social.plasma.relay.RelayMessage.EventRelayMessage
import social.plasma.relay.RelayMessage.NoticeRelayMessage
import social.plasma.relay.RequestMessage
import java.time.Instant
import java.util.*

class NostrMessageAdapter {

    // RequestMessage
    @ToJson
    fun requestMessageToJson(request: RequestMessage) =
        listOf("REQ", request.subscriptionId, Filters(Instant.now()))

    // RelayMessage
    @FromJson
    fun relayMessageFromJson(reader: JsonReader, eventDelegate: JsonAdapter<Event>): RelayMessage {
        reader.beginArray()
        val message = when (reader.nextString()) {
            "EVENT" -> EventRelayMessage(
                subscriptionId = reader.nextString(),
                event = eventDelegate.fromJson(reader)!!
            )
            "NOTICE" -> NoticeRelayMessage(reader.nextString())
            else -> throw java.lang.IllegalArgumentException()
        }
        reader.endArray()
        return message
    }

    @ToJson
    fun relayMessageToJson(
        writer: JsonWriter,
        message: RelayMessage,
        eventDelegate: JsonAdapter<Event>
    ) {
        when (message) {
            is NoticeRelayMessage -> {
                writer.beginArray()
                    .value("NOTICE")
                    .value(message.message)
                    .endArray()
            }
            is EventRelayMessage -> {
                writer.beginArray()
                    .value("EVENT")
                    .value(message.subscriptionId)
                eventDelegate.toJson(writer, message.event)
                writer.endArray()
            }
        }
    }


    // === primitives

    // Hex ByteString
    @FromJson
    fun byteStringFromJson(s: String): ByteString = s.decodeHex()

    @ToJson
    fun byteStringToJson(b: ByteString): String = b.hex()

    // Instant
    @FromJson
    fun instantFromJson(seconds: Long): Instant = Instant.ofEpochSecond(seconds)

    @ToJson
    fun instantToJson(i: Instant): Long = i.epochSecond

}