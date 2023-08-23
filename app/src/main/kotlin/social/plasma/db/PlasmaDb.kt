package social.plasma.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import social.plasma.data.daos.ContactsDao
import social.plasma.data.daos.EventsDao
import social.plasma.data.daos.HashtagDao
import social.plasma.data.daos.LastRequestDao
import social.plasma.data.daos.NotesDao
import social.plasma.data.daos.RelayInfoDao
import social.plasma.data.daos.UserMetadataDao
import social.plasma.db.converters.TagsTypeConverter
import social.plasma.models.ContactEntity
import social.plasma.models.LastRequestEntity
import social.plasma.models.RelayEntity
import social.plasma.models.UserMetadataEntity
import social.plasma.models.UserMetadataFtsEntity
import social.plasma.models.events.EventEntity
import social.plasma.models.events.EventReferenceEntity
import social.plasma.models.events.HashTagEntity
import social.plasma.models.events.HashTagFtsEntity
import social.plasma.models.events.HashTagReferenceEntity
import social.plasma.models.events.PubkeyReferenceEntity

@Database(
    entities = [
        EventEntity::class,
        LastRequestEntity::class,
        EventReferenceEntity::class,
        PubkeyReferenceEntity::class,
        HashTagReferenceEntity::class,
        HashTagEntity::class,
        HashTagFtsEntity::class,
        UserMetadataEntity::class,
        UserMetadataFtsEntity::class,
        ContactEntity::class,
        RelayEntity::class,
    ],
    version = 10,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 7, to = 8),
        AutoMigration(from = 8, to = 9),
        AutoMigration(from = 9, to = 10),
    ]
)
@TypeConverters(TagsTypeConverter::class)
abstract class PlasmaDb : RoomDatabase() {
    abstract fun contactsDao(): ContactsDao

    abstract fun eventsDao(): EventsDao
    abstract fun notesDao(): NotesDao
    abstract fun userMetadataDao(): UserMetadataDao
    abstract fun lastRequestDao(): LastRequestDao

    abstract fun hashtagDao(): HashtagDao

    abstract fun relayInfoDao(): RelayInfoDao
}
