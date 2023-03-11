package social.plasma.ui.components.richtext

import social.plasma.models.NoteId
import social.plasma.models.PubKey

sealed class Mention {
    abstract val text: String
}

data class NoteMention(override val text: String, val noteId: NoteId) : Mention()
data class ProfileMention(override val text: String, val pubkey: PubKey) : Mention()
