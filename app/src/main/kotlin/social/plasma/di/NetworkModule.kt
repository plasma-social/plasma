package social.plasma.di

import android.app.Application
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.messageadapter.moshi.MoshiMessageAdapter
import com.tinder.scarlet.streamadapter.rxjava2.RxJava2StreamAdapterFactory
import com.tinder.streamadapter.coroutines.CoroutinesStreamAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import social.plasma.nostr.relay.message.NostrMessageAdapter
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    @Named("default-relay-list")
    fun provideDefaultRelayList(): List<String> = listOf(
        "wss://brb.io",
        "wss://relay.damus.io",
        "wss://relay.nostr.bg",
        "wss://eden.nostr.land",
        "wss://nostr.oxtr.dev",
        "wss://nostr.v0l.io",
        "wss://nostr-pub.semisol.dev",
        "wss://relay.kronkltd.net",
        "wss://nostr.zebedee.cloud",
        "wss://no.str.cr",
        "wss://relay.nostr.info",
    )

    @Provides
    @Singleton
    fun providesOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .build()

    @Provides
    @Singleton
    fun providesMoshi(): Moshi = Moshi.Builder()
        .add(NostrMessageAdapter())
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun providesScarletBuilder(application: Application, moshi: Moshi): Scarlet.Builder =
        Scarlet.Builder()
            .addMessageAdapterFactory(MoshiMessageAdapter.Factory(moshi))
            .addStreamAdapterFactory(RxJava2StreamAdapterFactory())
            .addStreamAdapterFactory(CoroutinesStreamAdapterFactory())
//            .lifecycle(AndroidLifecycle.ofApplicationForeground(application))
}
