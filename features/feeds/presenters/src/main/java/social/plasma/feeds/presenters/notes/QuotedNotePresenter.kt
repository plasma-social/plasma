package social.plasma.feeds.presenters.notes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import app.cash.nostrino.crypto.PubKey
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.timeout
import okio.ByteString.Companion.decodeHex
import social.plasma.domain.interactors.SyncMetadata
import social.plasma.domain.observers.ObserveNote
import social.plasma.features.feeds.screens.feeditems.notes.NoteScreen
import social.plasma.features.feeds.screens.feeditems.notes.NoteUiState
import social.plasma.features.feeds.screens.feeditems.quotednotes.QuotedNoteEvent
import social.plasma.features.feeds.screens.feeditems.quotednotes.QuotedNoteScreen
import social.plasma.features.feeds.screens.feeditems.quotednotes.QuotedNoteUiState
import social.plasma.features.feeds.screens.threads.ThreadScreen
import social.plasma.features.profile.screens.ProfileScreen
import kotlin.time.Duration.Companion.seconds

private val timeout = 5.seconds

class QuotedNotePresenter @AssistedInject constructor(
    private val observeNote: ObserveNote,
    private val syncMetadata: SyncMetadata,
    private val notePresenterFactory: NotePresenter.Factory,
    @Assisted private val args: QuotedNoteScreen,
    @Assisted private val navigator: Navigator,
) : Presenter<QuotedNoteUiState> {
    private val noteFlow =
        observeNote.flow.timeout(timeout).filterNotNull()
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
                syncMetadata.executeSync(SyncMetadata.Params(PubKey(it.pubkey.decodeHex())))
            }
        }

        return when {
            noteNotFound -> QuotedNoteUiState.NoteNotFound
            note == null -> QuotedNoteUiState.Loading
            else -> {
                val noteUiState = remember(note) {
                    notePresenterFactory.create(
                        NoteScreen(note!!),
                        navigator
                    )
                }.present()

                noteUiState.toQuotedNoteUiState()
            }
        }
    }

    private fun NoteUiState.toQuotedNoteUiState(): QuotedNoteUiState {
        return when (this) {
            NoteUiState.Loading -> QuotedNoteUiState.Loading
            NoteUiState.NotFound -> QuotedNoteUiState.NoteNotFound
            is NoteUiState.Loaded -> QuotedNoteUiState.Loaded(
                note = noteCard,
            ) { event ->
                when (event) {
                    QuotedNoteEvent.OnAvatarClicked -> navigator.goTo(
                        ProfileScreen(noteCard.userPubkey.hex())
                    )

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



