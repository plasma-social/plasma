package social.plasma.nostr

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import social.plasma.nostr.relay.message.NostrMessageAdapter

object BuildingBlocks {

    val moshi = Moshi.Builder()
        .add(NostrMessageAdapter())
        .addLast(KotlinJsonAdapterFactory())
        .build()
}
