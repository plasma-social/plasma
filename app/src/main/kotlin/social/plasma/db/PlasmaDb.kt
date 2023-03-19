package social.plasma.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import social.plasma.data.daos.NotesDao
import social.plasma.data.daos.UserMetadataDao
import social.plasma.models.ContactEntity
import social.plasma.data.daos.ContactsDao
import social.plasma.db.converters.TagsTypeConverter
import social.plasma.models.events.EventEntity
import social.plasma.models.events.EventReferenceEntity
import social.plasma.data.daos.EventsDao
import social.plasma.data.daos.LastRequestDao
import social.plasma.models.events.PubkeyReferenceEntity
import social.plasma.models.NoteView
import social.plasma.models.LastRequestEntity
import social.plasma.models.UserMetadataEntity

@Database(
    entities = [
        EventEntity::class,
        LastRequestEntity::class,
        EventReferenceEntity::class,
        PubkeyReferenceEntity::class,
        UserMetadataEntity::class,
        ContactEntity::class,
    ],
    views = [
        NoteView::class,
    ],
    version = 3,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
    ]
)
@TypeConverters(TagsTypeConverter::class)
abstract class PlasmaDb : RoomDatabase() {
    abstract fun contactsDao(): ContactsDao

    abstract fun eventsDao(): EventsDao
    abstract fun notesDao(): NotesDao
    abstract fun userMetadataDao(): UserMetadataDao
    abstract fun lastRequestDao() : LastRequestDao
}
