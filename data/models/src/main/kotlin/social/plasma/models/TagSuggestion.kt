package social.plasma.models

import app.cash.nostrino.crypto.PubKey

data class TagSuggestion(
    val pubKey: PubKey,
    val imageUrl: String?,
    val title: String,
    val nip5Identifier: String?,
    val nip5Status: Nip5Status,
)
