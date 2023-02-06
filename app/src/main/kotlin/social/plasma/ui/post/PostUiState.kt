package social.plasma.ui.post

import social.plasma.db.notes.NoteWithUser

data class PostUiState(
    val postEnabled: Boolean = false,
    val parentNote: NoteWithUser? = null,
)
