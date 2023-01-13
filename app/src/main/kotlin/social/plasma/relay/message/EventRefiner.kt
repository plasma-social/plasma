package social.plasma.relay.message

import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import social.plasma.models.Event
import social.plasma.models.Note
import social.plasma.models.TypedEvent
import social.plasma.models.UserMetaData
import social.plasma.relay.message.RelayMessage.EventRelayMessage
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
/** Filters and refines an event relay message flow into a flows of more concrete event types */
class EventRefiner @Inject constructor(
    moshi: Moshi
) {

    private val userMetaDataAdapter = moshi.adapter(UserMetaData::class.java)

    fun toUserMetaData(messages: Flow<EventRelayMessage>): Flow<TypedEvent<UserMetaData>> =
        messages.filter { it.event.kind == Event.Kind.MetaData }
            .map { it.event to userMetaDataAdapter.fromJson(it.event.content) }
            .mapNotNull { (event, data) ->
                if (data == null) null
                else event.typed(data)
            }

    fun toNote(message: EventRelayMessage): TypedEvent<Note>? =
        if (message.event.kind == Event.Kind.Note) {
            message.event.typed(Note(message.event.content))
        } else null

}