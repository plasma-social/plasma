package social.plasma.features.feeds.screens.feeditems.notes

import android.os.Parcelable
import com.slack.circuit.runtime.Screen
import kotlinx.parcelize.Parcelize
import social.plasma.models.EventModel

@Parcelize
data class NoteScreen(
    val eventEntity: EventModel,
    val style: NoteStyle = NoteStyle.ElevatedCard,
) : Screen {
    sealed interface NoteStyle : Parcelable {
        @Parcelize
        object ElevatedCard : NoteStyle

        @Parcelize
        data class FlatCard(
            val showLineBreak: Boolean = true,
        ) : NoteStyle

        @Parcelize
        data class ThreadNote(
            val showConnector: Boolean = true,
        ) : NoteStyle
    }
}
