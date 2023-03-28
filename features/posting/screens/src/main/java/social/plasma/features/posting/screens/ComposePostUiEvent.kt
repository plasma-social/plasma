package social.plasma.features.posting.screens

import androidx.compose.ui.text.input.TextFieldValue
import com.slack.circuit.CircuitUiEvent
import social.plasma.models.TagSuggestion


sealed interface ComposePostUiEvent : CircuitUiEvent {
    data class OnNoteChange(val content: TextFieldValue) : ComposePostUiEvent
    object OnSubmitPost : ComposePostUiEvent
    object OnBackClick : ComposePostUiEvent

    data class OnSuggestionTapped(val suggestion: TagSuggestion) : ComposePostUiEvent
}
