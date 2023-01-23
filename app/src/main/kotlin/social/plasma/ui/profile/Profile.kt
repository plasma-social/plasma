package social.plasma.ui.profile

import android.app.Activity
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import coil.compose.AsyncImage
import social.plasma.R
import social.plasma.models.PubKey
import social.plasma.ui.components.Avatar
import social.plasma.ui.components.Nip5Badge
import social.plasma.ui.components.NoteCard
import social.plasma.ui.components.ProgressIndicator
import social.plasma.ui.components.StatCard
import social.plasma.ui.profile.ProfileUiState.Loaded.ProfileStat
import social.plasma.ui.profile.ProfileUiState.Loaded.UserData
import social.plasma.ui.theme.PlasmaTheme

@Composable
fun Profile(
    modifier: Modifier = Modifier,
    profileViewModel: ProfileViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
) {
    val uiState by profileViewModel.uiState.collectAsState(ProfileUiState.Loading)

    Profile(
        uiState = uiState,
        modifier = modifier,
        onNoteDisposed = profileViewModel::onNoteDisposed,
        onNoteDisplayed = profileViewModel::onNoteDisplayed,
        onNavigateBack = onNavigateBack,
    )
}

@Composable
private fun Profile(
    uiState: ProfileUiState,
    modifier: Modifier = Modifier,
    onNoteDisposed: (String) -> Unit,
    onNoteDisplayed: (String) -> Unit,
    onNavigateBack: () -> Unit,
) {
    when (uiState) {
        is ProfileUiState.Loading -> ProgressIndicator(modifier)
        is ProfileUiState.Loaded -> ProfileContent(
            uiState = uiState,
            onNoteDisplayed = onNoteDisplayed,
            onNoteDisposed = onNoteDisposed,
            modifier = modifier,
            onNavigateBack = onNavigateBack,
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ProfileContent(
    uiState: ProfileUiState.Loaded,
    onNoteDisplayed: (String) -> Unit,
    onNoteDisposed: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lazyPagingItems = uiState.userNotesPagingFlow.collectAsLazyPagingItems()

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.navigationBars,
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
        ) {
            item {
                ProfileAppBar(
                    avatarUrl = uiState.userData.avatarUrl,
                    onNavigateBack = onNavigateBack,
                    pubkey = uiState.userData.publicKey
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }

            item { ProfileBio(uiState.userData, following = uiState.following) }

            item { Spacer(modifier = Modifier.height(32.dp)) }

            item { ProfileStatsRow(uiState.statCards) }
            item { Spacer(modifier = Modifier.height(32.dp)) }

            items(lazyPagingItems) { cardUiModel ->
                cardUiModel?.let {
                    LaunchedEffect(Unit) {
                        onNoteDisplayed(cardUiModel.id)
                    }
                    NoteCard(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        uiModel = it,
                        onAvatarClick = null
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    DisposableEffect(Unit) {
                        onDispose {
                            onNoteDisposed(cardUiModel.id)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileAppBar(
    avatarUrl: String,
    pubkey: PubKey,
    onNavigateBack: () -> Unit,
) {
    val clipboardManager = LocalClipboardManager.current

    ConstraintLayout {
        val (coverImage, avatar, topAppBar) = createRefs()

        AsyncImage(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16 / 9f)
                .constrainAs(coverImage) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            model = "https://s3-alpha-sig.figma.com/img/4a90/2d76/b5f9770952063fd97aa73441dbeef396?Expires=1675036800&Signature=isHUrgxr-OJjU4HHfA~wfa-GTLIq~FT83RxqEurf13bTXwLykd-aHhsMXuLhx2Zqs-g5hCj4jM3355ngZlcY9qcrcrTgwcAxZLbwAhpntHl499McE9BU7aO7jG7j~eMy0Z7a~p3lFCHuQsyO7ukKZsawWVkCNtPdl8E-IQ~yxMc~LAB6QSlQlEJV7hIwBAbWgOKDgQ6spq-UFeoOee5Po02JCGtZOEb9vlxzFrhBKdCxCh1PdrX0~9Qb8rEeLGzAFzhJeOKJ0RYwzHsiGYGWsc1Ad9nvgoCXY2FwwIrixsxh3Jy87BivV4XCibvTE7YHhXwTRY29D-0Yun95GsHWWw__&Key-Pair-Id=APKAQ4GOSFWCVNEHN3O4",
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )

        TopAppBar(
            modifier = Modifier.constrainAs(topAppBar) {
                top.linkTo(parent.top, margin = 16.dp)
                start.linkTo(parent.start, margin = 16.dp)
                end.linkTo(parent.end, margin = 16.dp)
            },
            colors = TopAppBarDefaults.mediumTopAppBarColors(
                containerColor = Color.Transparent,
                navigationIconContentColor = Color.White, // TODO figure out best colors
                actionIconContentColor = Color.White
            ),
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        stringResource(R.string.back)
                    )
                }
            },
            actions = {
                IconButton(onClick = {
                    clipboardManager.setText(AnnotatedString(pubkey.bech32))
                }) {
                    Icon(
                        painterResource(R.drawable.ic_plasma_key),
                        stringResource(R.string.copy_public_key_to_clipboard)
                    )
                }
            },
            title = { },
        )

        Box(
            modifier = Modifier
                .size(92.dp)
                .border(4.dp, MaterialTheme.colorScheme.surface, CircleShape)
                .constrainAs(avatar) {
                    top.linkTo(coverImage.bottom)
                    bottom.linkTo(coverImage.bottom)
                    start.linkTo(parent.start, margin = 16.dp)
                },
            contentAlignment = Alignment.Center
        ) {
            Avatar(
                imageUrl = avatarUrl,
                contentDescription = null,
                size = 88.dp,
            )
        }
    }
}

@Composable
private fun ProfileBio(
    userData: UserData,
    following: Boolean?,
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    userData.petName,
                    style = MaterialTheme.typography.titleMedium,
                )
                userData.username?.let {
                    Text(
                        userData.username,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = { /*TODO*/ },
                enabled = following != null,
                border = ButtonDefaults.outlinedButtonBorder
                    .copy(brush = SolidColor(MaterialTheme.colorScheme.primary))
            ) {
                Icon(painterResource(R.drawable.ic_plasma_follow), null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    if (following == true) stringResource(id = R.string.following) else stringResource(
                        R.string.follow
                    )
                )
            }
        }



        userData.nip5?.let {
            Spacer(modifier = Modifier.height(8.dp))

            Nip5Badge(identifier = it)
        }

        userData.about?.let {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                it,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

    }
}

@Composable
private fun ProfileStatsRow(statCards: List<ProfileStat>) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        statCards.forEachIndexed { index, (label, value) ->
            StatCard(modifier = Modifier.weight(1f), label = label, value = value)

            if (index != statCards.lastIndex) {
                Spacer(modifier = Modifier.width(16.dp))
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun PreviewProfile(
    @PreviewParameter(ProfilePreviewProvider::class) uiState: ProfileUiState,
) {
    PlasmaTheme {
        Profile(
            uiState = uiState,
            onNoteDisplayed = {},
            onNoteDisposed = {},
            onNavigateBack = {},
        )
    }
}
