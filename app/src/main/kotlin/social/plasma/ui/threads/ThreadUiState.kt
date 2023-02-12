package social.plasma.ui.threads

import social.plasma.PubKey
import social.plasma.ui.components.notes.NoteUiModel

data class ThreadUiState(
    val noteUiModels: List<ThreadNoteUiModel>,
    val firstVisibleItem: Int = 0,
)

sealed interface ThreadNoteUiModel {
    val id: String
    val pubkey: PubKey

    data class RootNote(
        val noteUiModel: NoteUiModel,
        override val id: String = noteUiModel.id,
        override val pubkey: PubKey = noteUiModel.userPubkey,
    ) : ThreadNoteUiModel

    data class LeafNote(
        val noteUiModel: NoteUiModel,
        override val id: String = noteUiModel.id,
        override val pubkey: PubKey = noteUiModel.userPubkey,
        val showConnector: Boolean = true,
    ) : ThreadNoteUiModel
}
