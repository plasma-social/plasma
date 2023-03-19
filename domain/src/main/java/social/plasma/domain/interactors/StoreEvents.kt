package social.plasma.domain.interactors

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import social.plasma.data.daos.EventsDao
import social.plasma.domain.Interactor
import social.plasma.domain.SubjectInteractor
import social.plasma.models.Event
import social.plasma.models.events.EventEntity
import social.plasma.models.events.EventReferenceEntity
import social.plasma.models.events.PubkeyReferenceEntity
import social.plasma.shared.utils.real.chunked
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext
import kotlin.streams.toList

class StoreEvents @Inject constructor(
    private val eventsDao: EventsDao,
    @Named("io") private val ioDispatcher: CoroutineContext,
) : SubjectInteractor<Flow<Event>, Any>() {

    override fun createObservable(params: Flow<Event>): Flow<Any> {
        return params.map { it.toEventEntity() }
            .chunked(1000, 200)
            .onEach { events ->
                eventsDao.insert(events)
            }
            .flowOn(ioDispatcher)
    }
}

internal fun Event.toEventEntity() = EventEntity(
    id = id.hex(),
    pubkey = pubKey.hex(),
    createdAt = createdAt.epochSecond,
    kind = kind,
    tags = tags,
    content = content,
    sig = sig.hex(),
)
