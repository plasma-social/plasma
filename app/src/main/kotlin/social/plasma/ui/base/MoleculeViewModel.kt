package social.plasma.ui.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.AndroidUiDispatcher
import app.cash.molecule.RecompositionClock
import app.cash.molecule.launchMolecule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow

abstract class MoleculeViewModel<S, E>(
    private val recompositionClock: RecompositionClock,
) : ViewModel() {
    private val events = MutableSharedFlow<E>(extraBufferCapacity = 500)

    private val moleculeScope =
        CoroutineScope(viewModelScope.coroutineContext + AndroidUiDispatcher.Main)

    val uiState: StateFlow<S> by lazy(LazyThreadSafetyMode.NONE) {
        moleculeScope.launchMolecule(clock = recompositionClock) {
            models(events)
        }
    }

    fun onEvent(event: E) {
        if (!events.tryEmit(event)) {
            error("Event buffer overflow.")
        }
    }

    @Composable
    protected abstract fun models(events: Flow<E>): S
}

@Composable
inline fun <E> EventsEffect(
    events: Flow<E>,
    crossinline collector: CoroutineScope.(E) -> Unit,
) {
    LaunchedEffect(Unit) {
        events.collect {
            collector(it)
        }
    }
}
