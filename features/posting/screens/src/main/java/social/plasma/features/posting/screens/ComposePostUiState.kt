package social.plasma.features.posting.screens

import com.slack.circuit.CircuitUiState
import social.plasma.models.PubKey

data class ComposePostUiState(
    val postButtonEnabled: Boolean = false,
    val title: String = "",
    val placeholder: String = "",
    val postButtonLabel: String = "",
    val showTagSuggestions: Boolean = false,
    val tagSuggestions: List<TagSuggestion> = emptyList(),
    val onEvent: (ComposePostUiEvent) -> Unit,
) : CircuitUiState {
    data class TagSuggestion(
        val pubKey: PubKey,
        val imageUrl: String?,
        val title: String,
        val subtitle: String?,
    )
}

