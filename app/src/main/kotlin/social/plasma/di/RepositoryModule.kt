package social.plasma.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import social.plasma.repository.AccountStateRepository
import social.plasma.repository.RealAccountRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindAccountStateRepo(impl: RealAccountRepository): AccountStateRepository
}
