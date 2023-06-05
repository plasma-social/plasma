package social.plasma.features.discovery.presenters

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import com.slack.circuit.foundation.onNavEvent
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.onStart
import social.plasma.domain.observers.ObserveFollowedHashTags
import social.plasma.features.discovery.screens.communities.CommunityListUiEvent.OnChildNavEvent
import social.plasma.features.discovery.screens.communities.CommunityListUiState
import social.plasma.models.HashTag

class CommunityListPresenter @AssistedInject constructor(
    private val observeFollowedHashTags: ObserveFollowedHashTags,
    @Assisted private val navigator: Navigator,
) : Presenter<CommunityListUiState> {

    private val followedHashTagsFlow =
        observeFollowedHashTags.flow
            .onStart {
                observeFollowedHashTags(Unit)
            }
    private val recommendedHashTags = listOf(
        HashTag.parse("coffeechain"),
        HashTag.parse("foodchain"),
        HashTag.parse("foodstr"),
        HashTag.parse("grownostr"),
        HashTag.parse("plasma"),
        HashTag.parse("plebchain"),
        HashTag.parse("zap"),
    )


    @Composable
    override fun present(): CommunityListUiState {
        val followedHashTags by remember { followedHashTagsFlow }.collectAsState(initial = null)

        val recommendedHashTags by produceState(
            initialValue = emptyList(), followedHashTags
        ) {
            if (followedHashTags != null) {
                value = recommendedHashTags - followedHashTags!!.toSet()
            }
        }

        return CommunityListUiState(
            recommendedHashTags = recommendedHashTags,
            followedHashTags = followedHashTags ?: emptyList(),
        ) { event ->
            when (event) {
                is OnChildNavEvent -> navigator.onNavEvent(event.navEvent)
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(navigator: Navigator): CommunityListPresenter
    }
}
