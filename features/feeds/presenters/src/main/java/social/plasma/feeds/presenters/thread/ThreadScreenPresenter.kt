package social.plasma.feeds.presenters.thread

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.paging.PagingConfig
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import social.plasma.data.daos.NotesDao
import social.plasma.domain.interactors.SyncThreadEvents
import social.plasma.domain.observers.ObservePagedThreadFeed
import social.plasma.domain.observers.toEventModel
import social.plasma.features.feeds.presenters.R
import social.plasma.features.feeds.screens.eventfeed.EventFeedUiState
import social.plasma.features.feeds.screens.threads.ThreadScreen
import social.plasma.features.feeds.screens.threads.ThreadScreenUiEvent
import social.plasma.features.feeds.screens.threads.ThreadScreenUiState
import social.plasma.feeds.presenters.feed.ThreadFeedStateProducer
import social.plasma.shared.repositories.api.NoteRepository
import social.plasma.shared.utils.api.StringManager

class ThreadScreenPresenter @AssistedInject constructor(
    private val feedStateProducerFactory: ThreadFeedStateProducer.Factory,
    private val noteRepository: NoteRepository,
    private val observePagedThreadFeed: ObservePagedThreadFeed,
    private val notesDao: NotesDao,
    private val syncThreadEvents: SyncThreadEvents,
    private val stringManager: StringManager,
    @Assisted private val args: ThreadScreen,
    @Assisted private val navigator: Navigator,
) : Presenter<ThreadScreenUiState> {


    @Composable
    override fun present(): ThreadScreenUiState {
        val initialScrollIndex by produceState<Int?>(initialValue = null) {
            value = notesDao.getThreadNoteIndex(args.noteId.hex).dec().coerceAtLeast(0)
        }

        val pagingFlow = remember(initialScrollIndex) {
            val initialKey = initialScrollIndex ?: return@remember observePagedThreadFeed.flow

            observePagedThreadFeed.flow.onStart {
                observePagedThreadFeed(
                    ObservePagedThreadFeed.Params(
                        noteId = args.noteId,
                        pagingConfig = PagingConfig(
                            pageSize = 20,
                        ),
                        initialKey = initialKey,
                    )
                )
            }
        }

        LaunchedEffect(Unit) {
            syncThreadEvents.executeSync(SyncThreadEvents.Params(args.noteId))
        }

        val anchorNote by remember {
            noteRepository.observeEventById(args.noteId).map { it?.toEventModel() }
        }.collectAsState(initial = null)


        val feedStateProducer = remember(anchorNote, initialScrollIndex) {
            if (anchorNote == null || initialScrollIndex == null) return@remember null

            feedStateProducerFactory.create(
                eventModel = anchorNote!!,
                initialIndex = initialScrollIndex!!
            )
        }

        val feedUiState = feedStateProducer?.invoke(navigator, pagingFlow)

        return ThreadScreenUiState(
            title = stringManager[R.string.thread],
            eventFeedUiState = feedUiState ?: EventFeedUiState.Empty,
        ) { event ->
            when (event) {
                ThreadScreenUiEvent.OnBackClick -> navigator.pop()
            }
        }
    }


    @AssistedFactory
    interface Factory {
        fun create(args: ThreadScreen, navigator: Navigator): ThreadScreenPresenter
    }
}
