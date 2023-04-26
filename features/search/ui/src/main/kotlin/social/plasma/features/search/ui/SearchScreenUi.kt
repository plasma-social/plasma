package social.plasma.features.search.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.slack.circuit.Ui
import social.plasma.features.search.screens.SearchUiState

class SearchScreenUi : Ui<SearchUiState> {
    @Composable
    override fun Content(state: SearchUiState, modifier: Modifier) {
        Surface(modifier = modifier) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(horizontal = 16.dp),
            ) {
                Text("TODO")
            }
        }
    }
}
