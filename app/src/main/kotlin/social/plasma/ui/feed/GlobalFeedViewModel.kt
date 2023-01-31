package social.plasma.ui.feed

import app.cash.molecule.RecompositionClock
import dagger.hilt.android.lifecycle.HiltViewModel
import social.plasma.repository.NoteRepository
import social.plasma.repository.UserMetaDataRepository
import javax.inject.Inject

@HiltViewModel
class GlobalFeedViewModel @Inject constructor(
    recompositionClock: RecompositionClock,
    noteRepository: NoteRepository,
    userMetaDataRepository: UserMetaDataRepository,
) : AbstractFeedViewModel(
    recompositionClock = recompositionClock,
    userMetaDataRepository = userMetaDataRepository,
    pagingFlow = noteRepository.observeGlobalNotes()
)
