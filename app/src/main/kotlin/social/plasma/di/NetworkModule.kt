package social.plasma.di

import android.app.Application
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tinder.scarlet.Lifecycle
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.lifecycle.LifecycleRegistry
import com.tinder.scarlet.messageadapter.moshi.MoshiMessageAdapter
import com.tinder.scarlet.retry.ExponentialBackoffStrategy
import com.tinder.scarlet.streamadapter.rxjava2.RxJava2StreamAdapterFactory
import com.tinder.streamadapter.coroutines.CoroutinesStreamAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BASIC
import okhttp3.logging.HttpLoggingInterceptor.Level.BODY
import social.plasma.BuildConfig.DEBUG
import social.plasma.nostr.relay.message.NostrMessageAdapter
import social.plasma.utils.ApplicationResumedLifecycle
import social.plasma.utils.ConnectivityOnLifecycle
import javax.inject.Named
import javax.inject.Singleton
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    @Named("default-relay-list")
    fun provideDefaultRelayList(): List<String> = listOf(
        "wss://relay.damus.io",
        "wss://nos.lol",
        "wss://eden.nostr.land",
        "wss://nostr.oxtr.dev",
        "wss://no.str.cr",
        "wss://relay.nostr.band",
        "wss://relay.snort.social",
        "wss://puravida.nostr.land",
        "wss://atlas.nostr.land",
    )

    @Provides
    @Singleton
    fun providesOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().setLevel(if (DEBUG) BODY else BASIC))
        .build()

    @Provides
    @Singleton
    fun providesMoshi(): Moshi = Moshi.Builder()
        .add(NostrMessageAdapter())
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun providesLifecycle(application: Application): Lifecycle {
        return ApplicationResumedLifecycle(application, LifecycleRegistry(500))
            .combineWith(ConnectivityOnLifecycle(application))
    }

    @Provides
    @Singleton
    fun providesScarletBuilder(lifecycle: Lifecycle, moshi: Moshi): Scarlet.Builder =
        Scarlet.Builder()
            .addMessageAdapterFactory(MoshiMessageAdapter.Factory(moshi))
            .addStreamAdapterFactory(RxJava2StreamAdapterFactory())
            .addStreamAdapterFactory(CoroutinesStreamAdapterFactory())
            .backoffStrategy(
                ExponentialBackoffStrategy(
                    5.seconds.inWholeMilliseconds,
                    30.minutes.inWholeMilliseconds
                )
            )
            .lifecycle(lifecycle)
}

