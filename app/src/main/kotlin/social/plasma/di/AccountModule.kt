package social.plasma.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import social.plasma.prefs.Preference
import social.plasma.prefs.UserKeyPreference

@Module
@InstallIn(SingletonComponent::class)
abstract class AccountModule {

    @Binds
    @UserKey
    abstract fun providesUserKeyPref(userKeyPreference: UserKeyPreference): Preference<String>
}
