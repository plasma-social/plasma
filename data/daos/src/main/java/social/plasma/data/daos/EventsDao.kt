package social.plasma.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import social.plasma.models.events.EventEntity
import social.plasma.models.events.EventReferenceEntity
import social.plasma.models.events.HashTagEntity
import social.plasma.models.events.HashTagReferenceEntity
import social.plasma.models.events.PubkeyReferenceEntity

@Dao
abstract class EventsDao {
    @Transaction
    open suspend fun insert(events: List<EventEntity>) {
        coroutineScope {
            val noteReferences = async {
                events.flatMap { event ->
                    event.tags.filter { it.firstOrNull() == "e" }.map { tag ->
                        EventReferenceEntity(
                            sourceEvent = event.id,
                            targetEvent = tag[1],
                            relayUrl = tag.getOrNull(2),
                            marker = tag.getOrNull(3)?.toMarkerOrNull(),
                        )
                    }
                }
            }

            val pubkeyReferences = async {
                events.flatMap { event ->
                    event.tags.filter { it.firstOrNull() == "p" }.map {
                        PubkeyReferenceEntity(
                            event.id,
                            pubkey = it[1],
                            relayUrl = it.getOrNull(2)
                        )
                    }
                }
            }

            val hashtagReferences = async {
                events.flatMap { event ->
                    event.tags.filter { it.firstOrNull() == "t" && it.size > 1 }.map {
                        HashTagReferenceEntity(
                            sourceEvent = event.id,
                            hashtag = it[1].lowercase(),
                            pubkey = event.pubkey,
                        )
                    }
                }
            }

            insertInternal(events)
            insertEventReferences(noteReferences.await())
            insertPubkeyReferences(pubkeyReferences.await())

            val hashTagReferences = hashtagReferences.await()
            val hashTagEntities =
                hashTagReferences.map { HashTagEntity(hashtag = it.hashtag) }.distinct()

            insertHashTagReferences(hashTagReferences)
            insertHashTags(hashTagEntities)
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    internal abstract fun insertHashTags(tags: List<HashTagEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    internal abstract fun insertHashTagReferences(references: List<HashTagReferenceEntity>)

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

    /**
     * Purges events that where not created by the given pubkey,
     * or that do not make a reference to the given pubkey.
     *
     * @param excludedPubkey The pubkey to exclude from the purge.
     * @param keepCount The maximum number of events to keep outside of the excluded pubkey.
     */
    @Query(
        """DELETE FROM events WHERE pubkey != :excludedPubkey 
        AND id NOT IN (SELECT source_event FROM pubkey_ref WHERE pubkey = :excludedPubkey)
        AND id NOT IN (SELECT id FROM events WHERE pubkey != :excludedPubkey ORDER BY created_at DESC LIMIT :keepCount)"""
    )
    abstract suspend fun purgeEvents(excludedPubkey: String, keepCount: Int)

}
