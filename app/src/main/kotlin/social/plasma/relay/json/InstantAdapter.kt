package social.plasma.relay.json

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.Instant

class InstantAdapter {
    @FromJson
    fun fromJson(seconds: Long): Instant = Instant.ofEpochSecond(seconds)
    @ToJson
    fun toJson(i: Instant): Long = i.epochSecond
}