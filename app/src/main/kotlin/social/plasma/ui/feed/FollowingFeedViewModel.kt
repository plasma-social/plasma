package social.plasma.ui.feed

import app.cash.molecule.RecompositionClock
import dagger.hilt.android.lifecycle.HiltViewModel
import social.plasma.opengraph.OpenGraphParser
import social.plasma.repository.AccountStateRepository
import social.plasma.repository.NoteRepository
import social.plasma.repository.ReactionsRepository
import social.plasma.repository.UserMetaDataRepository
import social.plasma.ui.mappers.NotePagingFlowMapper
import javax.inject.Inject

@HiltViewModel
class FollowingFeedViewModel @Inject constructor(
    recompositionClock: RecompositionClock,
    noteRepository: NoteRepository,
    userMetaDataRepository: UserMetaDataRepository,
    reactionsRepository: ReactionsRepository,
    notePagingFlowMapper: NotePagingFlowMapper,
    openGraphParser: OpenGraphParser,
    accountStateRepo: AccountStateRepository,
) : AbstractFeedViewModel(
    recompositionClock = recompositionClock,
    userMetaDataRepository = userMetaDataRepository,
    reactionsRepository = reactionsRepository,
    pagingFlow = noteRepository.observeContactsNotes(),
    notePagingFlowMapper = notePagingFlowMapper,
    openGraphParser = openGraphParser,
    accountStateRepo = accountStateRepo,
)

