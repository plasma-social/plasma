package social.plasma.features.feeds.screens.threads

import com.slack.circuit.Screen
import kotlinx.parcelize.Parcelize
import social.plasma.models.NoteId

@Parcelize
data class ThreadScreen(
    val noteId: NoteId,
) : Screen
