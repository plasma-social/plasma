package social.plasma.domain.interactors

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import social.plasma.data.daos.EventsDao
import social.plasma.domain.SubjectInteractor
import social.plasma.models.Event
import social.plasma.models.events.EventEntity
import social.plasma.shared.utils.real.chunked
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext

class StoreEvents @Inject constructor(
    private val eventsDao: EventsDao,
    @Named("io") private val ioDispatcher: CoroutineContext,
) : SubjectInteractor<Flow<Event>, List<EventEntity>>() {

    override fun createObservable(params: Flow<Event>): Flow<List<EventEntity>> {
        return params.map { it.toEventEntity() }
            .chunked(500, 500)
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
