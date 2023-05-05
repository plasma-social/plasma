package social.plasma.features.posting.screens

import androidx.compose.ui.text.input.TextFieldValue
import com.slack.circuit.CircuitUiEvent
import social.plasma.models.TagSuggestion


sealed interface CreatePostUiEvent : CircuitUiEvent {
    data class OnNoteChange(val content: TextFieldValue) : CreatePostUiEvent
    object OnSubmitPost : CreatePostUiEvent
    object OnBackClick : CreatePostUiEvent

    data class OnUserSuggestionTapped(val suggestion: TagSuggestion) : CreatePostUiEvent

    data class OnHashTagSuggestionTapped(val hashtag: String) : CreatePostUiEvent
}
