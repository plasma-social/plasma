package social.plasma.features.feeds.screens.feeditems.quotednotes

import com.slack.circuit.runtime.Screen
import kotlinx.parcelize.Parcelize
import social.plasma.models.NoteId

@Parcelize
data class QuotedNoteScreen(val noteId: NoteId) : Screen
