package social.plasma.nostr

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import social.plasma.nostr.relay.Relay
import social.plasma.nostr.relay.Relays
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NostrModule {
    @Singleton
    @Binds
    abstract fun bindsRelay(impl: Relays): Relay

    companion object {
        fun <T> Flow<T>.tap(f: (T) -> Unit): Flow<T> = this.map { f(it); it }
    }
}

