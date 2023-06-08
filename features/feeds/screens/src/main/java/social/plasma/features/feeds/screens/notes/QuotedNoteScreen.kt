package social.plasma.features.feeds.screens.notes

import com.slack.circuit.runtime.Screen
import kotlinx.parcelize.Parcelize
import social.plasma.models.NoteId

@Parcelize
data class QuotedNoteScreen(val noteId: NoteId) : Screen
