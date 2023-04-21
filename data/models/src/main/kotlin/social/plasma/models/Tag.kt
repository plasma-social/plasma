package social.plasma.models

import app.cash.nostrino.crypto.PubKey

sealed interface Tag
data class EventTag(val noteId: NoteId) : Tag
data class PubKeyTag(val pubKey: PubKey) : Tag
data class HashTag(val tag: String) : Tag
