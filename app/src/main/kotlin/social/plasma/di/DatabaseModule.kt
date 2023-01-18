package social.plasma.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import social.plasma.db.PlasmaDb
import social.plasma.db.notes.NoteDao
import social.plasma.db.reactions.ReactionDao
import social.plasma.db.usermetadata.UserMetadataDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun providesDb(applicationContext: Application): PlasmaDb {
        return Room.databaseBuilder(
            applicationContext,
            PlasmaDb::class.java, "plasmadb"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun providesNotesDao(db: PlasmaDb): NoteDao = db.noteDao()

    @Provides
    fun providesUserMetadataDao(db: PlasmaDb): UserMetadataDao = db.userMetadataDao()

    @Provides
    fun providesReactionsDao(db: PlasmaDb): ReactionDao = db.reactionsDao()
}
