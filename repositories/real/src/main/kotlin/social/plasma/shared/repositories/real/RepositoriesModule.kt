package social.plasma.shared.repositories.real

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import social.plasma.shared.repositories.api.AccountStateRepository
import social.plasma.shared.repositories.api.ContactsRepository
import social.plasma.shared.repositories.api.LightningAddressResolver
import social.plasma.shared.repositories.api.LightningInvoiceFetcher
import social.plasma.shared.repositories.api.Nip5Validator
import social.plasma.shared.repositories.api.NoteRepository
import social.plasma.shared.repositories.api.UserMetadataRepository
import social.plasma.shared.utils.api.Preference
import social.plasma.shared.utils.real.prefs.ByteArrayPreference.ByteArrayPreferenceFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoriesModule {
    @Binds
    @Singleton
    internal abstract fun bindProvides(impl: RealNoteRepository): NoteRepository

    @Binds
    @Singleton
    internal abstract fun bindsMetadataRepository(impl: RealUserMetadataRepository): UserMetadataRepository

    @Binds
    @Singleton
    internal abstract fun bindsRealNip5Validator(impl: RealNip5Validator): Nip5Validator

    @Binds
    @Singleton
    internal abstract fun providesAccountStateRepo(impl: RealAccountRepository): AccountStateRepository

    @Binds
    @Singleton
    internal abstract fun bindsContactRepository(impl: RealContactsRepository): ContactsRepository

    @Binds
    @Singleton
    internal abstract fun bindsLightningInvoiceFetcher(impl: RealLightningInvoiceFetcher): LightningInvoiceFetcher

    @Binds
    @Singleton
    internal abstract fun bindsLightningAddressResolver(impl: RealLightningUrlResolver): LightningAddressResolver

    companion object {
        @Provides
        @UserKey(KeyType.Secret)
        fun providesSecretKey(factory: ByteArrayPreferenceFactory): Preference<ByteArray> =
            factory.create("secret_key")

        @Provides
        @UserKey(KeyType.Public)
        fun providesPublicKey(factory: ByteArrayPreferenceFactory): Preference<ByteArray> =
            factory.create("public_key")
    }
}
