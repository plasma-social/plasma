package social.plasma.data.nostr.fakes

import app.cash.nostrino.crypto.PubKey
import app.cash.nostrino.crypto.SecKey
import okio.ByteString
import social.plasma.nostr.models.ZapRequestProvider
import java.util.UUID

class FakeZapRequestProvider : ZapRequestProvider {
    var fakeZapRequestValue = UUID.randomUUID().toString()
    override fun getSignedZapRequest(
        secKey: SecKey,
        recipient: PubKey,
        amount: Long,
        eventId: ByteString?,
    ): String = fakeZapRequestValue
}
