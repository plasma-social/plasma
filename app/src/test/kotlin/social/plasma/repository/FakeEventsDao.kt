package social.plasma.repository

import app.cash.turbine.Turbine
import social.plasma.db.events.EventEntity
import social.plasma.db.events.EventReferenceEntity
import social.plasma.db.events.EventsDao
import social.plasma.db.events.PubkeyReferenceEntity

class FakeEventsDao : EventsDao {
    val eventsByIdTurbine = Turbine<EventEntity?>()

    override suspend fun insert(events: Iterable<EventEntity>) {
        TODO("Not yet implemented")
    }

    override suspend fun insertEventReferences(references: Iterable<EventReferenceEntity>) {
        TODO("Not yet implemented")
    }

    override suspend fun insertPubkeyReferences(references: Iterable<PubkeyReferenceEntity>) {
        TODO("Not yet implemented")
    }

    override suspend fun getById(id: String): EventEntity? {
        return eventsByIdTurbine.awaitItem()
    }

}
