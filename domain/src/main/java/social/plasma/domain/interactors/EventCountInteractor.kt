package social.plasma.domain.interactors

import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import social.plasma.domain.ResultInteractor
import social.plasma.nostr.relay.RelayManager
import social.plasma.nostr.relay.message.ClientMessage

/**
 * Interactor that subscribes to a relay and returns the count of events for a given subscription message.
 */
abstract class EventCountInteractor<in P>(
    private val relayManager: RelayManager,
) : ResultInteractor<P, EventCountInteractor.Result>() {
    override suspend fun doWork(params: P): Result {
        val subscribeMessage = getSubscribeMessage(params)
        return relayManager.countMessages
            .filter { subscribeMessage.subscriptionId == it.subscriptionId }
            .map { Result.Success(it.count.count) }
            .onStart { relayManager.sendCountRequest(subscribeMessage) }
            .catch { Result.Failure }
            .first()
    }

    abstract suspend fun getSubscribeMessage(params: P): ClientMessage.SubscribeMessage

    sealed interface Result {
        data class Success(val count: Long) : Result
        object Failure : Result
    }
}
