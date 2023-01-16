package social.plasma.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import social.plasma.repository.AccountStateRepository
import social.plasma.repository.NoteRepository
import social.plasma.repository.RealAccountRepository
import social.plasma.repository.RealNoteRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindNotesRepository(impl: RealNoteRepository): NoteRepository

    @Binds
    abstract fun bindAccountStateRepo(impl: RealAccountRepository): AccountStateRepository
}
