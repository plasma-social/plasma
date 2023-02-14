package social.plasma.ui.feed

import app.cash.molecule.RecompositionClock
import dagger.hilt.android.lifecycle.HiltViewModel
import social.plasma.opengraph.OpenGraphParser
import social.plasma.repository.NoteRepository
import social.plasma.repository.ReactionsRepository
import social.plasma.repository.UserMetaDataRepository
import social.plasma.ui.mappers.NotePagingFlowMapper
import javax.inject.Inject

@HiltViewModel
class RepliesFeedViewModel @Inject constructor(
    recompositionClock: RecompositionClock,
    noteRepository: NoteRepository,
    userMetaDataRepository: UserMetaDataRepository,
    reactionsRepository: ReactionsRepository,
    notePagingFlowMapper: NotePagingFlowMapper,
    openGraphParser: OpenGraphParser,
) : AbstractFeedViewModel(
    recompositionClock = recompositionClock,
    userMetaDataRepository = userMetaDataRepository,
    reactionsRepository = reactionsRepository,
    pagingFlow = noteRepository.observeContactsNotesAndReplies(),
    notePagingFlowMapper = notePagingFlowMapper,
    openGraphParser = openGraphParser,
)
