package social.plasma.features.posting.screens

import androidx.compose.ui.text.input.TextFieldValue
import com.slack.circuit.CircuitUiEvent
import social.plasma.models.TagSuggestion


sealed interface ComposePostUiEvent : CircuitUiEvent {
    data class OnNoteChange(val content: TextFieldValue) : ComposePostUiEvent
    object OnSubmitPost : ComposePostUiEvent
    object OnBackClick : ComposePostUiEvent

    data class OnUserSuggestionTapped(val suggestion: TagSuggestion) : ComposePostUiEvent

    data class OnHashTagSuggestionTapped(val hashtag: String) : ComposePostUiEvent
}
