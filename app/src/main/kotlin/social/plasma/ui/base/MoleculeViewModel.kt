package social.plasma.ui.base

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.AndroidUiDispatcher
import app.cash.molecule.RecompositionClock
import app.cash.molecule.launchMolecule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

abstract class MoleculeViewModel<S>(
    private val recompositionClock: RecompositionClock,
) : ViewModel() {

    private val moleculeScope =
        CoroutineScope(viewModelScope.coroutineContext + AndroidUiDispatcher.Main)

    // TODO add some kind of event flow hookup
    fun uiState(): StateFlow<S> = moleculeScope.launchMolecule(recompositionClock) {
        models()
    }

    @Composable
    protected abstract fun models(): S
}
