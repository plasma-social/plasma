package social.plasma.ui.post

sealed interface PostUiEvent {
    data class OnNoteChange(val content: String) : PostUiEvent
    object PostNote : PostUiEvent
}
