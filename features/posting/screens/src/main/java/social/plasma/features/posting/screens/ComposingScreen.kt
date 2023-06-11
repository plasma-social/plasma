package social.plasma.features.posting.screens

import android.os.Parcelable
import com.slack.circuit.runtime.Screen
import kotlinx.parcelize.Parcelize
import social.plasma.models.NoteId

@Parcelize
data class ComposingScreen(
    val content: String = "",
    val noteType: NoteType = NoteType.NewPost,
) : Screen {
    sealed interface NoteType : Parcelable {

        @Parcelize
        data class Reply(val originalNote: NoteId) : NoteType

        @Parcelize
        data class Repost(val originalNote: NoteId) : NoteType

        @Parcelize
        object NewPost : NoteType
    }
}
