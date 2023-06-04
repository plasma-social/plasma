package social.plasma.domain.interactors

import social.plasma.models.Event
import social.plasma.models.HashTag
import social.plasma.nostr.relay.RelayManager
import social.plasma.nostr.relay.message.ClientMessage
import social.plasma.nostr.relay.message.Filter
import javax.inject.Inject

class GetHashTagFollowerCount @Inject constructor(
    relayManager: RelayManager,
) : EventCountInteractor<GetHashTagFollowerCount.Params>(relayManager) {

    data class Params(
        val hashtag: HashTag,
    )

    override suspend fun getSubscribeMessage(params: Params): ClientMessage.SubscribeMessage {
        return ClientMessage.SubscribeMessage(
            Filter(
                kinds = setOf(Event.Kind.ContactList),
                hashTags = setOf(params.hashtag.name)
            ),
        )
    }
}

