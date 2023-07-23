package social.plasma.nostr

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import social.plasma.nostr.models.RealZapRequestProvider
import social.plasma.nostr.models.ZapRequestProvider
import social.plasma.nostr.relay.RealRelayManager
import social.plasma.nostr.relay.RelayManager
import social.plasma.nostr.relay.message.EventRefiner
import social.plasma.nostr.relay.message.RealEventRefiner
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NostrModule {

    @Singleton
    @Binds
    internal abstract fun bindsEventRefined(impl: RealEventRefiner): EventRefiner

    @Singleton
    @Binds
    internal abstract fun bindsRelayManager(impl: RealRelayManager): RelayManager

    @Binds
    internal abstract fun bindsZapRequestProvider(impl: RealZapRequestProvider): ZapRequestProvider
}

