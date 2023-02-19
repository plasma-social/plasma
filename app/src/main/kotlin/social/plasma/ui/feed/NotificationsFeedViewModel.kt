package social.plasma.ui.feed

import androidx.lifecycle.viewModelScope
import app.cash.molecule.RecompositionClock
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import social.plasma.models.PubKey
import social.plasma.opengraph.OpenGraphParser
import social.plasma.repository.AccountStateRepository
import social.plasma.repository.NoteRepository
import social.plasma.repository.ReactionsRepository
import social.plasma.repository.UserMetaDataRepository
import social.plasma.ui.mappers.NotePagingFlowMapper

@HiltViewModel
class NotificationsFeedViewModel @javax.inject.Inject constructor(
    recompositionClock: RecompositionClock,
    noteRepository: NoteRepository,
    userMetaDataRepository: UserMetaDataRepository,
    reactionsRepository: ReactionsRepository,
    notePagingFlowMapper: NotePagingFlowMapper,
    accountStateRepository: AccountStateRepository,
    openGraphParser: OpenGraphParser,
) : AbstractFeedViewModel(
    recompositionClock = recompositionClock,
    userMetaDataRepository = userMetaDataRepository,
    reactionsRepository = reactionsRepository,
    pagingFlow = noteRepository.observeMentions(),
    notePagingFlowMapper = notePagingFlowMapper,
    openGraphParser = openGraphParser,
) {
    val pubkey = PubKey.of(accountStateRepository.getPublicKey()!!)

    val userMetadataState = userMetaDataRepository.observeUserMetaData(pubkey.hex)
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
}
