package social.plasma.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import social.plasma.models.TypedEvent
import social.plasma.models.UserMetaData
import social.plasma.relay.Relays
import social.plasma.relay.message.EventRefiner
import social.plasma.relay.message.Filters
import social.plasma.relay.message.SubscribeMessage
import social.plasma.relay.message.UnsubscribeMessage
import javax.inject.Inject

interface UserMetaDataRepository {
    fun observeUserMetaData(): Flow<List<TypedEvent<UserMetaData>>>
}

class RealUserMetaDataRepository @Inject constructor(
    private val relays: Relays,
    private val eventRefiner: EventRefiner,
) : UserMetaDataRepository {

    fun requestUserMetaData(pubKey: String): List<UnsubscribeMessage> =
        relays.subscribe(SubscribeMessage(
            filters = Filters.userMetaData(pubKey)
        ))

    private val userMetaDataSharedFlow: SharedFlow<List<TypedEvent<UserMetaData>>> =
        relays.sharedFlow { eventRefiner.toUserMetaData(it) }

    override fun observeUserMetaData(): Flow<List<TypedEvent<UserMetaData>>> {
        return userMetaDataSharedFlow
    }
}
