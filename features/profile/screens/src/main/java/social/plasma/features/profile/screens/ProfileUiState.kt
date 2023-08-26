package social.plasma.features.profile.screens

import app.cash.nostrino.crypto.PubKey
import com.slack.circuit.runtime.CircuitUiState
import social.plasma.features.feeds.screens.eventfeed.EventFeedUiState

sealed interface ProfileUiState : CircuitUiState {
    object Loading : ProfileUiState
    data class Loaded(
        val feedState: EventFeedUiState,
        val statCards: List<ProfileStat>,
        val userData: UserData,
        val following: Boolean? = false,
        val isNip5Valid: Boolean = false,
        val showLightningIcon: Boolean = false,
        val onEvent: (ProfileUiEvent) -> Unit,
    ) : ProfileUiState {
        data class ProfileStat(
            val label: String,
            val value: String,
        )

        data class UserData(
            val banner: String,
            val website: String?,
            val displayName: String,
            val username: String?,
            val publicKey: PubKey,
            val about: String?,
            val avatarUrl: String?,
            val nip5Identifier: String?,
            val nip5Domain: String? = null,
        )
    }
}
