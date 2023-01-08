package social.plasma.relay.json

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import okio.ByteString
import okio.ByteString.Companion.decodeHex

class HexByteStringAdapter {
    @FromJson
    fun fromJson(s: String): ByteString = s.decodeHex()
    @ToJson
    fun toJson(b: ByteString): String = b.hex()
}