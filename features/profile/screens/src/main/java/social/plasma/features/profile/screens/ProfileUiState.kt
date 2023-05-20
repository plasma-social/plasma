package social.plasma.features.profile.screens

import app.cash.nostrino.crypto.PubKey
import com.slack.circuit.runtime.CircuitUiState
import social.plasma.features.feeds.screens.feed.FeedUiState

sealed interface ProfileUiState : CircuitUiState {
    object Loading : ProfileUiState
    data class Loaded(
        val feedState: FeedUiState,
        val statCards: List<ProfileStat>,
        val userData: UserData,
        val following: Boolean? = false,
        val isNip5Valid: Boolean = false,
        val onEvent: (ProfileUiEvent) -> Unit,
    ) : ProfileUiState {
        data class ProfileStat(
            val label: String,
            val value: String,
        )

        data class UserData(
            val banner: String,
            val website: String?,
            val petName: String,
            val username: String?,
            val publicKey: PubKey,
            val about: String?,
            val avatarUrl: String?,
            val nip5Identifier: String?,
            val nip5Domain: String? = null,
            val lud: String?,
        )
    }
}
