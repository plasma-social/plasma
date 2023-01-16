package social.plasma.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import social.plasma.prefs.ByteArrayPreference.ByteArrayPreferenceFactory
import social.plasma.prefs.Preference

@Module
@InstallIn(SingletonComponent::class)
object AccountModule {

    @Provides
    @UserKey(KeyType.Secret)
    fun providesSecretKey(factory: ByteArrayPreferenceFactory): Preference<ByteArray> =
        factory.create("secret_key")

    @Provides
    @UserKey(KeyType.Public)
    fun providesPublicKey(factory: ByteArrayPreferenceFactory): Preference<ByteArray> =
        factory.create("public_key")
}
