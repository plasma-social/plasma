package social.plasma.features.feeds.screens.feeditems.notes

import com.slack.circuit.runtime.Screen
import kotlinx.parcelize.Parcelize
import social.plasma.models.EventModel

@Parcelize
data class NoteScreen(val eventEntity: EventModel) : Screen
