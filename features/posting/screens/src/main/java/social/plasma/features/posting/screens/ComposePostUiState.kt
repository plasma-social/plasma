package social.plasma.features.posting.screens

import androidx.compose.ui.text.input.TextFieldValue
import com.slack.circuit.CircuitUiState
import social.plasma.models.ProfileMention
import social.plasma.models.TagSuggestion

data class ComposePostUiState(
    val postButtonEnabled: Boolean = false,
    val title: String = "",
    val placeholder: String = "",
    val postButtonLabel: String = "",
    val showAutoComplete: Boolean = false,
    val autoCompleteSuggestions: List<AutoCompleteSuggestion> = emptyList(),
    val noteContent: TextFieldValue,
    val mentions: Map<String, ProfileMention> = emptyMap(),
    val onEvent: (ComposePostUiEvent) -> Unit,
) : CircuitUiState

data class AutoCompleteSuggestion(
    val tagSuggestion: TagSuggestion,
    val nip5Valid: Boolean? = null,
)
