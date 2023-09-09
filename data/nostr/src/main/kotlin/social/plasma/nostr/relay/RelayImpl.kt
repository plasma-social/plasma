package social.plasma.nostr.relay

import app.cash.nostrino.client.ConnectionState
import app.cash.nostrino.client.RelayClient
import app.cash.nostrino.client.Subscription
import app.cash.nostrino.crypto.SecKey
import app.cash.nostrino.message.relay.EndOfStoredEvents
import app.cash.nostrino.message.relay.Notice
import app.cash.nostrino.message.relay.RelayMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import social.plasma.nostr.relay.message.ClientMessage.EventMessage
import social.plasma.nostr.relay.message.ClientMessage.SubscribeMessage
import social.plasma.nostr.relay.message.ClientMessage.UnsubscribeMessage
import social.plasma.nostr.relay.message.RelayMessage.EOSEMessage
import social.plasma.nostr.relay.message.RelayMessage.EventRelayMessage
import social.plasma.nostr.relay.message.RelayMessage.NoticeRelayMessage
import timber.log.Timber
import java.time.Instant
import app.cash.nostrino.message.relay.EventMessage as NostrinoEventMessage
import app.cash.nostrino.model.Event as NostrinoEvent
import app.cash.nostrino.model.Filter as NostrinoFilter
import social.plasma.models.Event as PlasmaEvent
import social.plasma.nostr.relay.message.Filter as PlasmaFilter
import social.plasma.nostr.relay.message.RelayMessage as PlasmaRelayMessage

class RelayImpl(
    override val url: String,
    override val canRead: Boolean = true,
    override val canWrite: Boolean = true,
    override val supportedNips: Set<Nip> = emptySet(),
) : Relay {

    private val relayClient = RelayClient(url)

    private val logger
        get() = Timber.tag("relay-$url")

    override val connectionStatus: Flow<Relay.RelayStatus> =
        relayClient.connectionState.map { it.toPlasmaStatus() }

    override val relayMessages =
        relayClient.relayMessages.map { it.toPlasmaRelayMessage() }

    override fun subscribe(subscribeMessage: SubscribeMessage) {
        if (!canRead) {
            logger.i("Not allowed to read")
            return
        }

        relayClient.subscribe(
            subscribeMessage.filters.toNostrinoFilters(),
            Subscription(subscribeMessage.subscriptionId)
        )
    }


    override suspend fun send(event: EventMessage) {
        if (!canWrite) {
            logger.i("Not allowed to write")
            return
        }

        relayClient.send(event.event.toNostrinoEvent())
    }


    override suspend fun sendNote(
        text: String,
        secKey: SecKey,
        tags: Set<List<String>>,
    ) = send(createEventMessage(text, secKey, tags))

    override fun sendCountRequest(subscribeMessage: SubscribeMessage) {
        if (supportedNips.contains(Nip.EventCount)) {
            logger.d("requesting count for %s", subscribeMessage)
            relayClient.subscribe(
                subscribeMessage.filters.toNostrinoFilters(),
                Subscription(subscribeMessage.subscriptionId)
            )
        } else {
            throw UnsupportedOperationException("This relay does not support Event Counts. https://nips.be/${Nip.EventCount}")
        }
    }

    private fun createEventMessage(
        text: String,
        secKey: SecKey,
        tags: Set<List<String>>,
    ) = EventMessage(
        PlasmaEvent.createEvent(
            secKey.pubKey.key,
            secKey.key,
            Instant.now(),
            PlasmaEvent.Kind.Note,
            tags.toList(),
            text
        )
    )

    override fun unsubscribe(unsubscribeMessage: UnsubscribeMessage) {
        relayClient.unsubscribe(Subscription(unsubscribeMessage.subscriptionId))
    }


    override fun connect() {
        relayClient.start()
    }


    override fun disconnect() {
        relayClient.stop()
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RelayImpl

        if (url != other.url) return false
        if (canRead != other.canRead) return false
        return canWrite == other.canWrite
    }

    override fun hashCode(): Int {
        var result = url.hashCode()
        result = 31 * result + canRead.hashCode()
        result = 31 * result + canWrite.hashCode()
        return result
    }

    private fun ConnectionState.toPlasmaStatus(): Relay.RelayStatus {

        val status = when (this) {
            ConnectionState.Connected -> Relay.Status.Connected
            ConnectionState.Disconnected -> Relay.Status.Initial
            ConnectionState.Failing -> Relay.Status.ConnectionFailed(RuntimeException("Connection Failing"))
            else -> Relay.Status.Initial
        }

        return Relay.RelayStatus(url, status)
    }
}


private fun RelayMessage.toPlasmaRelayMessage(): PlasmaRelayMessage = when (this) {
    is NostrinoEventMessage -> EventRelayMessage(
        subscriptionId = this.subscriptionId,
        event = this.event.toPlasmasEvent()
    )

    is EndOfStoredEvents -> EOSEMessage
    is Notice -> NoticeRelayMessage(this.message)
    // TODO add count relay messages
    else -> throw IllegalArgumentException("Unknown RelayMessage type: $this")
}

private fun NostrinoEvent.toPlasmasEvent(): PlasmaEvent = PlasmaEvent(
    content = this.content,
    pubKey = this.pubKey,
    kind = this.kind,
    tags = this.tags,
    createdAt = this.createdAt,
    id = this.id,
    sig = this.sig,
)

private fun PlasmaEvent.toNostrinoEvent(): NostrinoEvent = NostrinoEvent(
    content = this.content,
    pubKey = this.pubKey,
    kind = this.kind,
    tags = this.tags,
    createdAt = this.createdAt,
    id = this.id,
    sig = this.sig,
)

private fun List<PlasmaFilter>.toNostrinoFilters(): Set<NostrinoFilter> {
    return this.map {
        NostrinoFilter(
            ids = it.ids,
            authors = it.authors,
            eTags = it.eTags,
            pTags = it.pTags,
            tTags = it.hashTags,
            since = it.since,
            kinds = it.kinds,
            limit = it.limit,
        )
    }.toSet()
}
