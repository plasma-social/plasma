package social.plasma.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import social.plasma.models.events.EventEntity
import social.plasma.models.events.EventReferenceEntity
import social.plasma.models.events.PubkeyReferenceEntity
import kotlin.streams.toList

@Dao
abstract class EventsDao {
    @Transaction
    open suspend fun insert(events: List<EventEntity>) {
        val noteReferences = events.parallelStream().flatMap { event ->
            event.tags.parallelStream().filter { it.firstOrNull() == "e" }.map { tag ->
                EventReferenceEntity(
                    sourceEvent = event.id,
                    targetEvent = tag[1],
                    relayUrl = tag.getOrNull(2),
                    marker = tag.getOrNull(3)?.toMarkerOrNull(),
                )
            }
        }

        val pubkeyReferences = events.parallelStream().flatMap { event ->
            event.tags.parallelStream().filter { it.firstOrNull() == "p" }.map {
                PubkeyReferenceEntity(
                    event.id,
                    pubkey = it[1],
                    relayUrl = it.getOrNull(2)
                )
            }
        }

        insertEventReferences(noteReferences.toList())
        insertPubkeyReferences(pubkeyReferences.toList())
        insertInternal(events)
    }

    private fun String?.toMarkerOrNull(): EventReferenceEntity.EventMarker? = when (this) {
        "reply" -> EventReferenceEntity.EventMarker.Reply
        "mention" -> EventReferenceEntity.EventMarker.Mention
        "root" -> EventReferenceEntity.EventMarker.Root
        else -> null
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    internal abstract suspend fun insertInternal(events: Iterable<EventEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    internal abstract suspend fun insertEventReferences(references: Iterable<EventReferenceEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    internal abstract suspend fun insertPubkeyReferences(references: Iterable<PubkeyReferenceEntity>)

    @Query("SELECT * FROM events WHERE id = :id")
    abstract suspend fun getById(id: String): EventEntity?
}
