package social.plasma.features.search.presenters

import androidx.compose.runtime.Composable
import com.slack.circuit.Navigator
import com.slack.circuit.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import social.plasma.features.search.screens.SearchUiState

class SearchScreenPresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
) : Presenter<SearchUiState> {
    @Composable
    override fun present(): SearchUiState {
        return SearchUiState(onEvent = {

        })
    }


    @AssistedFactory
    interface Factory {
        fun create(navigator: Navigator): SearchScreenPresenter
    }
}
