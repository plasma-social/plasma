package social.plasma.nostr

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import social.plasma.nostr.relay.Relay
import social.plasma.nostr.relay.Relays
import social.plasma.nostr.relay.message.EventRefiner
import social.plasma.nostr.relay.message.RealEventRefiner
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NostrModule {
    @Singleton
    @Binds
    abstract fun bindsRelay(impl: Relays): Relay

    @Singleton
    @Binds
    internal abstract fun bindsEventRefined(impl: RealEventRefiner): EventRefiner
}

