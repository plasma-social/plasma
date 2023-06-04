package social.plasma.models

import android.os.Parcelable
import app.cash.nostrino.crypto.PubKey
import kotlinx.parcelize.Parcelize

sealed interface Tag
data class EventTag(val noteId: NoteId) : Tag
data class PubKeyTag(val pubKey: PubKey) : Tag

@Parcelize
data class HashTag private constructor(val tag: String) : Tag, Parcelable {
    val name get() = tag.lowercase()
    val displayName get() = "#$name"

    companion object {
        fun parse(tag: String): HashTag {
            return HashTag(tag.removePrefix("#"))
        }
    }
}
