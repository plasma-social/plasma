package social.plasma.feeds.presenters.notes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.timeout
import social.plasma.domain.interactors.SyncMetadata
import social.plasma.domain.observers.ObserveNote
import social.plasma.features.feeds.screens.feed.FeedItem
import social.plasma.features.feeds.screens.feeditems.quotednotes.QuotedNoteEvent
import social.plasma.features.feeds.screens.feeditems.quotednotes.QuotedNoteScreen
import social.plasma.features.feeds.screens.feeditems.quotednotes.QuotedNoteUiState
import social.plasma.features.feeds.screens.threads.ThreadScreen
import social.plasma.features.profile.screens.ProfileScreen
import social.plasma.feeds.presenters.feed.NoteCardMapper
import kotlin.time.Duration.Companion.seconds

private val timeout = 5.seconds

class QuotedNotePresenter @AssistedInject constructor(
    private val observeNote: ObserveNote,
    private val syncMetadata: SyncMetadata,
    private val noteCardMapper: NoteCardMapper,
    @Assisted private val args: QuotedNoteScreen,
    @Assisted private val navigator: Navigator,
) : Presenter<QuotedNoteUiState> {

    private val noteFlow =
        observeNote.flow.timeout(timeout).filterNotNull()
            .mapNotNull { noteCardMapper.toFeedItem(it) as? FeedItem.NoteCard }
            .onStart {
                observeNote(ObserveNote.Params(args.noteId))
            }

    @Composable
    override fun present(): QuotedNoteUiState {
        val note by remember { noteFlow }.collectAsState(null)
        val noteNotFound by produceState(initialValue = false, note) {
            delay(timeout)
            value = note == null
        }

        LaunchedEffect(note) {
            note?.let {
                syncMetadata.executeSync(SyncMetadata.Params(it.userPubkey))
            }
        }

        return when {
            noteNotFound -> QuotedNoteUiState.NoteNotFound
            note == null -> QuotedNoteUiState.Loading
            else -> QuotedNoteUiState.Loaded(
                note = note!!,
            ) { event ->
                when (event) {
                    QuotedNoteEvent.OnAvatarClicked -> navigator.goTo(ProfileScreen(note!!.userPubkey.hex()))
                    QuotedNoteEvent.OnNoteClicked -> navigator.goTo(ThreadScreen(args.noteId))
                }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(args: QuotedNoteScreen, navigator: Navigator): QuotedNotePresenter
    }
}

