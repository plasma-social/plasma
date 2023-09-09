package social.plasma.features.discovery.ui.search

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.slack.circuit.runtime.ui.Ui
import social.plasma.features.discovery.screens.search.HashTagSearchSuggestionItem
import social.plasma.features.discovery.screens.search.HashTagSearchSuggestionItem.SuggestionIcon
import social.plasma.features.discovery.screens.search.SearchBarUiState
import social.plasma.features.discovery.screens.search.SearchSuggestion
import social.plasma.features.discovery.screens.search.SearchUiEvent
import social.plasma.features.discovery.screens.search.SearchUiEvent.OnSearchSuggestionTapped
import social.plasma.features.discovery.screens.search.SearchUiState
import social.plasma.features.discovery.screens.search.UserSearchItem
import social.plasma.features.discovery.ui.R
import social.plasma.ui.components.Avatar
import social.plasma.ui.components.Nip5Badge
import social.plasma.ui.theme.PlasmaTheme
import social.plasma.ui.R as UiR

class SearchScreenUi : Ui<SearchUiState> {
    @Composable
    override fun Content(state: SearchUiState, modifier: Modifier) {
        SearchScreenContent(state, modifier)
    }
}


@Composable
private fun SearchScreenContent(state: SearchUiState, modifier: Modifier = Modifier) {
    val onEvent = state.onEvent

    val bottomPadding by animateDpAsState(
        targetValue = if (state.searchBarUiState.isActive) 0.dp else 24.dp,
        label = "bottomPadding"
    )

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            SearchResultContainer(
                modifier = Modifier.padding(bottom = bottomPadding),
                state = state.searchBarUiState,
                onEvent = onEvent,
            )
        }
//        Recommendations(
//            modifier = Modifier
//                .fillMaxSize()
//                .weight(1f)
//        )
    }
}

@Composable
private fun Recommendations(
    modifier: Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text(
                "RECOMMENDED",
                modifier = Modifier.padding(bottom = 16.dp),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        items(10) {
            Card {
                ListItem(
                    modifier = Modifier.padding(8.dp),
                    leadingContent = {

                        Image(
                            painter = painterResource(id = R.drawable.foodstr),
                            contentDescription = "Foodstr $it",
                            modifier = Modifier
                                .size(50.dp)
                                .clip(MaterialTheme.shapes.medium),
                            contentScale = ContentScale.Crop
                        )
                    },
                    headlineContent = {
                        Row(
                            modifier = Modifier.padding(bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Default.RestaurantMenu,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text(
                                "Foodstr $it",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    },
                    supportingContent = {
                        Text(
                            "Culinary influencers of nostr",
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                )
            }
            Spacer(modifier = Modifier.padding(8.dp))
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
private fun SearchResultContainer(
    state: SearchBarUiState,
    onEvent: (SearchUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    SearchBar(
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
        LazyColumn {
            state.searchSuggestionGroups.forEach { group ->
                group.title?.let { title ->
                    SectionHeader(title)
                }

                items(group.items) { item ->
                    SearchSuggestionItem(
                        item = item,
                        onEvent = onEvent,
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchSuggestionItem(item: SearchSuggestion, onEvent: (SearchUiEvent) -> Unit) {
    when (item) {
        is HashTagSearchSuggestionItem -> ListItem(
            modifier = Modifier.clickable { onEvent(OnSearchSuggestionTapped(item)) },
            leadingContent = item.icon?.let { { SuggestionIcon(it) } },
            headlineContent = {
                Text(
                    text = item.content,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent,
            ),
        )

        is UserSearchItem -> UserSearchSuggestionItem(item, onEvent)
    }
}

@Composable
fun UserSearchSuggestionItem(suggestion: UserSearchItem, onEvent: (SearchUiEvent) -> Unit) {
    ListItem(
        modifier = Modifier.clickable {
            onEvent(
                OnSearchSuggestionTapped(
                    suggestion
                )
            )
        },
        headlineContent = {
            Text(suggestion.title)
        },
        supportingContent = {
            Nip5Badge(suggestion.nip5Status)
        },
        leadingContent = {
            Avatar(
                imageUrl = suggestion.imageUrl, contentDescription = null
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
        ),
    )
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
private fun LazyListScope.SectionHeader(title: String) {
    stickyHeader {
        AnimatedContent(targetState = title, label = title) {
            Text(
                it,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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

            is SearchBarUiState.TrailingIcon.Avatar -> Avatar(
                size = 32.dp,
                imageUrl = trailingIcon.url,
                contentDescription = null,
                onClick = onClick,
            )

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
                    painterResource(id = UiR.drawable.ic_chevron_back),
                    "Back",
                )
            }

            SearchBarUiState.LeadingIcon.Search -> IconButton(onClick = onClick) {
                Icon(
                    painterResource(id = UiR.drawable.ic_plasma_search),
                    "Search",
                )
            }
        }
    }

}

@Composable
private fun SuggestionIcon(icon: SuggestionIcon) {
    when (icon) {
        SuggestionIcon.Recent -> Icon(
            Icons.Default.AccessTime,
            "Recent"
        )

        SuggestionIcon.Popular -> Icon(
            Icons.Default.TrendingUp,
            "Popular"
        )
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

