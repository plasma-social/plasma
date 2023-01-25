package social.plasma.db

import androidx.room.Database
import androidx.room.RoomDatabase
import social.plasma.db.contacts.ContactEntity
import social.plasma.db.contacts.ContactsDao
import social.plasma.db.notes.NoteDao
import social.plasma.db.notes.NoteEntity
import social.plasma.db.notes.NoteView
import social.plasma.db.reactions.ReactionDao
import social.plasma.db.reactions.ReactionEntity
import social.plasma.db.usermetadata.UserMetadataDao
import social.plasma.db.usermetadata.UserMetadataEntity

@Database(
    entities = [
        NoteEntity::class,
        UserMetadataEntity::class,
        ReactionEntity::class,
        ContactEntity::class,
    ],
    views = [
        NoteView::class,
    ],
    version = 1
)
abstract class PlasmaDb : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    abstract fun userMetadataDao(): UserMetadataDao
    abstract fun reactionsDao(): ReactionDao

    abstract fun contactsDao(): ContactsDao
}
