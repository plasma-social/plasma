package social.plasma.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import social.plasma.data.daos.NotesDao
import social.plasma.data.daos.UserMetadataDao
import social.plasma.db.PlasmaDb
import social.plasma.data.daos.ContactsDao
import social.plasma.db.converters.TagsTypeConverter
import social.plasma.data.daos.EventsDao
import social.plasma.data.daos.LastRequestDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun providesDb(
        applicationContext: Application,
        tagsTypeConverter: TagsTypeConverter,
    ): PlasmaDb {
        return Room.databaseBuilder(
            applicationContext,
            PlasmaDb::class.java, "plasmadb"
        )
            .addTypeConverter(tagsTypeConverter)
            .fallbackToDestructiveMigration()
            .build()
    }
    @Provides
    @Singleton
    fun providesContactsDao(db: PlasmaDb): ContactsDao = db.contactsDao()

    @Provides
    @Singleton
    fun providesEventDao(db: PlasmaDb): EventsDao = db.eventsDao()

    @Provides
    @Singleton
    fun providesNotesDao(db: PlasmaDb) : NotesDao = db.notesDao()

    @Provides
    @Singleton
    fun providesUserMetadataDao(db: PlasmaDb) : UserMetadataDao = db.userMetadataDao()

    @Provides @Singleton
    fun providesLastRequestDao(db: PlasmaDb) : LastRequestDao = db.lastRequestDao()
}
