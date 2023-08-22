package social.plasma.feeds.presenters.feed

import androidx.compose.runtime.Composable
import androidx.paging.PagingData
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.Screen
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.Flow
import social.plasma.features.feeds.screens.eventfeed.EventFeedUiState
import social.plasma.features.feeds.screens.feeditems.notes.NoteScreen
import social.plasma.feeds.presenters.eventfeed.EventFeedDefaults
import social.plasma.feeds.presenters.eventfeed.EventFeedPresenter
import social.plasma.models.EventModel

class ThreadFeedStateProducer @AssistedInject constructor(
    private val feedPresenterFactory: EventFeedPresenter.Factory,
    @Assisted private val anchorNote: EventModel,
) : FeedStateProducer {

    private val screenProvider: (EventModel) -> Screen = { model ->
        when (model.kind) {
            in EventFeedDefaults.noteCardKinds -> {
                if (model.id == anchorNote.id) {
                    NoteScreen(
                        model,
                        style = NoteScreen.NoteStyle.FlatCard()
                    )
                } else {
                    NoteScreen(
                        model,
                        style = NoteScreen.NoteStyle.ThreadNote(
                            showConnector = model.createdAt < anchorNote.createdAt
                        )
                    )
                }
            }

            else -> throw IllegalArgumentException("Unsupported event model kind: ${model.kind}")
        }
    }

    @Composable
    override fun invoke(
        navigator: Navigator,
        pagingFlow: Flow<PagingData<EventModel>>,
    ): EventFeedUiState = feedPresenterFactory.create(
        navigator,
        pagingFlow,
        screenProvider = screenProvider
    ).present()

    @AssistedFactory
    interface Factory {
        fun create(
            eventModel: EventModel,
        ): ThreadFeedStateProducer
    }
}
