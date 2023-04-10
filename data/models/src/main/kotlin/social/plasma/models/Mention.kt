package social.plasma.models

import app.cash.nostrino.crypto.PubKey
sealed class Mention {
    abstract val text: String
}

data class NoteMention(override val text: String, val noteId: NoteId) : Mention()
data class ProfileMention(override val text: String, val pubkey: PubKey) : Mention()
