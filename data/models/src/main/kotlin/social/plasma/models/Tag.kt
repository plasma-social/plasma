package social.plasma.models

sealed interface Tag
data class EventTag(val noteId: NoteId) : Tag
data class PubKeyTag(val pubKey: PubKey) : Tag