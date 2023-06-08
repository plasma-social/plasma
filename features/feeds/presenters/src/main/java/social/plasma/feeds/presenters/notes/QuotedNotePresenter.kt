package social.plasma.feeds.presenters.notes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onStart
import social.plasma.domain.interactors.SyncMetadata
import social.plasma.domain.observers.ObserveNote
import social.plasma.features.feeds.screens.feed.FeedItem
import social.plasma.features.feeds.screens.notes.QuotedNoteEvent
import social.plasma.features.feeds.screens.notes.QuotedNoteScreen
import social.plasma.features.feeds.screens.notes.QuotedNoteUiState
import social.plasma.features.feeds.screens.threads.ThreadScreen
import social.plasma.features.profile.screens.ProfileScreen
import social.plasma.feeds.presenters.feed.NoteCardMapper

class QuotedNotePresenter @AssistedInject constructor(
    private val observeNote: ObserveNote,
    private val syncMetadata: SyncMetadata,
    private val noteCardMapper: NoteCardMapper,
    @Assisted private val args: QuotedNoteScreen,
    @Assisted private val navigator: Navigator,
) : Presenter<QuotedNoteUiState> {

    private val noteFlow =
        observeNote.flow.filterNotNull()
            .mapNotNull { noteCardMapper.toFeedItem(it) as? FeedItem.NoteCard }
            .onStart {
                observeNote(ObserveNote.Params(args.noteId))
            }

    @Composable
    override fun present(): QuotedNoteUiState {
        val note by remember { noteFlow }.collectAsState(null)

        LaunchedEffect(note) {
            note?.let {
                syncMetadata.executeSync(SyncMetadata.Params(it.userPubkey))
            }
        }

        return QuotedNoteUiState(
            note = note,
        ) { event ->
            when (event) {
                QuotedNoteEvent.OnAvatarClicked -> navigator.goTo(ProfileScreen(note!!.userPubkey.hex()))
                QuotedNoteEvent.OnNoteClicked -> navigator.goTo(ThreadScreen(args.noteId))
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(args: QuotedNoteScreen, navigator: Navigator): QuotedNotePresenter
    }
}
