package social.plasma.nostr.models

import app.cash.nostrino.crypto.PubKey
import app.cash.nostrino.crypto.SecKey
import app.cash.nostrino.message.NostrMessageAdapter.Companion.moshi
import app.cash.nostrino.model.Event
import app.cash.nostrino.model.ZapRequest
import okio.ByteString
import social.plasma.nostr.relay.RelayManager
import javax.inject.Inject

interface ZapRequestProvider {
    fun getSignedZapRequest(
        secKey: SecKey,
        recipient: PubKey,
        amount: Long,
        eventId: ByteString? = null,
    ): String
}

class RealZapRequestProvider @Inject constructor(
    private val relayManager: RelayManager,
) : ZapRequestProvider {
    override fun getSignedZapRequest(
        secKey: SecKey,
        recipient: PubKey,
        amount: Long,
        eventId: ByteString?,
    ): String {
        val relayUrls =
            relayManager.relayUrls.value // TODO: This should be the recipient's read relays.

        val zapRequestEvent = ZapRequest(
            content = "",
            relays = relayUrls,
            amount = amount,
            lnurl = null,
            eventId = eventId,
            to = recipient,
        ).sign(secKey)

        return moshi.adapter(Event::class.java).toJson(zapRequestEvent)
    }
}
