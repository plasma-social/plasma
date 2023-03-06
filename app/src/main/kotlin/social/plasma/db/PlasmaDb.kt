package social.plasma.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import social.plasma.db.contacts.ContactEntity
import social.plasma.db.contacts.ContactsDao
import social.plasma.db.converters.TagsTypeConverter
import social.plasma.db.events.EventEntity
import social.plasma.db.events.EventReferenceEntity
import social.plasma.db.events.EventsDao
import social.plasma.db.events.PubkeyReferenceEntity
import social.plasma.db.notes.NoteDao
import social.plasma.db.notes.NoteView
import social.plasma.db.reactions.ReactionDao
import social.plasma.db.usermetadata.UserMetadataDao
import social.plasma.db.usermetadata.UserMetadataEntity

@Database(
    entities = [
        EventEntity::class,
        EventReferenceEntity::class,
        PubkeyReferenceEntity::class,
        UserMetadataEntity::class,
        ContactEntity::class,
    ],
    views = [
        NoteView::class,
    ],
    version = 1
)
@TypeConverters(TagsTypeConverter::class)
abstract class PlasmaDb : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    abstract fun userMetadataDao(): UserMetadataDao
    abstract fun reactionsDao(): ReactionDao

    abstract fun contactsDao(): ContactsDao

    abstract fun eventsDao(): EventsDao
}
