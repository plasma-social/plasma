package social.plasma.features.feeds.screens.hashtags

import com.slack.circuit.runtime.CircuitUiState
import social.plasma.features.feeds.screens.feed.FeedUiState

data class HashTagScreenUiState(
    val title: String,
    val feedState: FeedUiState,
    val followButtonUiState: ButtonUiState,
    val onEvent: (HashTagScreenUiEvent) -> Unit,
) : CircuitUiState

data class ButtonUiState(
    val label: String,
    val enabled: Boolean = true,
    val style: Style = Style.Primary,
) {
    enum class Style {
        Primary,
        PrimaryOutline,
    }
}


