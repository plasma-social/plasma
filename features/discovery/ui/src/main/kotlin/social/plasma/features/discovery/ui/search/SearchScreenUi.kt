package social.plasma.features.discovery.ui.search

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.slack.circuit.Ui
import social.plasma.features.discovery.screens.search.SearchBarUiState
import social.plasma.features.discovery.screens.search.SearchUiEvent
import social.plasma.features.discovery.screens.search.SearchUiState
import social.plasma.features.discovery.screens.search.Suggestion
import social.plasma.ui.R
import social.plasma.ui.theme.PlasmaTheme

class SearchScreenUi : Ui<SearchUiState> {
    @Composable
    override fun Content(state: SearchUiState, modifier: Modifier) {
        SearchScreenContent(state, modifier)
    }
}



@Composable
private fun SearchScreenContent(state: SearchUiState, modifier: Modifier = Modifier) {
    val onEvent = state.onEvent

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Surface {
            SearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                state = state.searchBarUiState,
                onEvent = onEvent,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SearchBar(
    state: SearchBarUiState,
    onEvent: (SearchUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    DockedSearchBar(
        modifier = modifier,
        query = state.query,
        onQueryChange = { onEvent(SearchUiEvent.OnQueryChanged(it)) },
        onSearch = { onEvent(SearchUiEvent.OnSearch) },
        active = state.isActive,
        onActiveChange = { onEvent(SearchUiEvent.OnActiveChanged(it)) },
        colors = SearchBarDefaults.colors(
            containerColor = MaterialTheme.colorScheme.background,
            inputFieldColors = TextFieldDefaults.textFieldColors(
                containerColor = MaterialTheme.colorScheme.onBackground,
            )
        ),
        placeholder = { Text("Find people and communities") },
        leadingIcon = {
            LeadingIcon(
                state.leadingIcon,
                onClick = { onEvent(SearchUiEvent.OnLeadingIconTapped) })
        },
        trailingIcon = {
            TrailingIcon(
                state.trailingIcon,
                onClick = { onEvent(SearchUiEvent.OnTrailingIconTapped) })
        },
    ) {
        state.suggestionsTitle?.let {
            Text(
                it,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        LazyColumn {
            items(state.suggestions) { suggestion ->
                ListItem(
                    leadingContent = suggestion.icon?.let { { SuggestionIcon(it) } },
                    headlineContent = {
                        Text(
                            text = suggestion.content,
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent,
                    ),
                )
            }
        }
    }
}

@Composable
private fun TrailingIcon(trailingIcon: SearchBarUiState.TrailingIcon?, onClick: () -> Unit) {
    AnimatedVisibility(
        visible = trailingIcon != null,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        when (trailingIcon) {
            SearchBarUiState.TrailingIcon.Clear -> IconButton(onClick = onClick) {
                Icon(
                    Icons.Default.Close,
                    "Clear",
                )
            }

            null -> Unit
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun LeadingIcon(leadingIcon: SearchBarUiState.LeadingIcon, onClick: () -> Unit) {
    AnimatedContent(
        targetState = leadingIcon,
        label = "leadingIcon",
        transitionSpec = {
            fadeIn() with fadeOut()
        }
    ) { icon ->
        when (icon) {
            SearchBarUiState.LeadingIcon.Back -> IconButton(onClick = onClick) {
                Icon(
                    painterResource(id = R.drawable.ic_chevron_back),
                    "Back",
                )
            }

            SearchBarUiState.LeadingIcon.Search -> IconButton(onClick = onClick) {
                Icon(
                    painterResource(id = R.drawable.ic_plasma_search),
                    "Search",
                )
            }
        }
    }

}

@Composable
private fun SuggestionIcon(icon: Suggestion.SuggestionIcon) {
    when (icon) {
        Suggestion.SuggestionIcon.Recent -> Icon(Icons.Default.AccessTime, "Recent")
    }
}


@Composable
@Preview
private fun SearchScreenContentPreview(
    @PreviewParameter(SearchScreenPreviewProvider::class) uiState: SearchUiState,
) {
    PlasmaTheme(darkTheme = true) {
        SearchScreenContent(uiState)
    }
}

