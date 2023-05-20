package social.plasma.features.posting.screens

import com.slack.circuit.runtime.Screen
import kotlinx.parcelize.Parcelize
import social.plasma.models.NoteId

@Parcelize
data class ComposingScreen(
    val content: String = "",
    val parentNote: NoteId? = null,
) : Screen
