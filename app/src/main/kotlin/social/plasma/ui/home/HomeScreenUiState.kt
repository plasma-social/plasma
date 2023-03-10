package social.plasma.ui.home

import social.plasma.models.PubKey
import social.plasma.nostr.models.UserMetaData


sealed interface HomeScreenUiState {
    data class Loaded(
        val userMetadata: UserMetaData,
        val userPubkey: PubKey,
    ) : HomeScreenUiState
}
