package social.plasma.data.daos.fakes

import social.plasma.data.daos.EventsDao
import social.plasma.models.events.EventEntity
import social.plasma.models.events.EventReferenceEntity
import social.plasma.models.events.HashTagEntity
import social.plasma.models.events.HashTagReferenceEntity
import social.plasma.models.events.PubkeyReferenceEntity

class FakeEventsDao : EventsDao() {
    override fun insertHashTags(tags: List<HashTagEntity>) {
        TODO("Not yet implemented")
    }

    override fun insertHashTagReferences(references: List<HashTagReferenceEntity>) {
        TODO("Not yet implemented")
    }

    override suspend fun insertInternal(events: Iterable<EventEntity>) {
        TODO("Not yet implemented")
    }

    override suspend fun insertEventReferences(references: Iterable<EventReferenceEntity>) {
        TODO("Not yet implemented")
    }

    override suspend fun insertPubkeyReferences(references: Iterable<PubkeyReferenceEntity>) {
        TODO("Not yet implemented")
    }

    override suspend fun getById(id: String): EventEntity? {
        TODO("Not yet implemented")
    }

    override suspend fun purgeEvents(excludedPubkey: String, keepCount: Int) {
        TODO("Not yet implemented")
    }
}
