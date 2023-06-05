package social.plasma.domain.interactors

import app.cash.nostrino.crypto.PubKey
import social.plasma.models.Event
import social.plasma.nostr.relay.RelayManager
import social.plasma.nostr.relay.message.ClientMessage
import social.plasma.nostr.relay.message.Filter
import javax.inject.Inject

class GetPubkeyFollowerCount @Inject constructor(
    relayManager: RelayManager,
) : EventCountInteractor<GetPubkeyFollowerCount.Params>(relayManager) {

    data class Params(
        val pubkey: PubKey,
    )

    override suspend fun getSubscribeMessage(params: Params): ClientMessage.SubscribeMessage {
        return ClientMessage.SubscribeMessage(
            Filter(
                kinds = setOf(Event.Kind.ContactList),
                pTags = setOf(params.pubkey.hex())
            ),
        )
    }
}

