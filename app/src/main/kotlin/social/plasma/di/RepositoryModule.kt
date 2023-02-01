package social.plasma.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import social.plasma.repository.AccountStateRepository
import social.plasma.repository.ContactListRepository
import social.plasma.repository.NoteRepository
import social.plasma.repository.ReactionsRepository
import social.plasma.repository.RealAccountRepository
import social.plasma.repository.RealContactListRepository
import social.plasma.repository.RealNoteRepository
import social.plasma.repository.RealReactionsRepository
import social.plasma.repository.RealUserMetaDataRepository
import social.plasma.repository.UserMetaDataRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAccountStateRepo(impl: RealAccountRepository): AccountStateRepository

    @Binds
    @Singleton
    abstract fun bindNoteRepository(impl: RealNoteRepository): NoteRepository

    @Binds
    @Singleton
    abstract fun bindUserMetadataRepository(impl: RealUserMetaDataRepository): UserMetaDataRepository

    @Binds
    @Singleton
    abstract fun bindContactListRepository(impl: RealContactListRepository): ContactListRepository

    @Binds
    @Singleton
    abstract fun bindReactionsRepo(impl: RealReactionsRepository) : ReactionsRepository
}
