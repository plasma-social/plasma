package social.plasma.models

import okio.ByteString
import okio.ByteString.Companion.decodeHex
import java.lang.IllegalArgumentException

data class Contact(
    val pubKey: ByteString,
    val homeRelayUrl: String? = null,
    val petName: String? = null,
) {
    companion object {
        private fun String.pubKeyOrNull(): ByteString? =
            try { this.decodeHex() }
            catch (_: IllegalArgumentException) { null }

        fun fromTag(tag: List<String>): Contact? =
            if ((2..4).contains(tag.size)) {
                val (label, pubKeyString, relayUrl, petName) = when (tag.size) {
                    2 -> tag.plus("").plus("")
                    3 -> tag.plus("")
                    else -> tag
                }

                val pubKey = pubKeyString.pubKeyOrNull()
                if (label != "p") null
                else pubKey?.let {
                    Contact(it, relayUrl.ifEmpty { null }, petName.ifEmpty { null })
                }
            } else null
    }
}