package social.plasma.ui.profile

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import social.plasma.models.NoteId
import social.plasma.models.PubKey
import social.plasma.ui.R
import social.plasma.ui.components.ConfirmationDialog
import social.plasma.ui.components.Nip5Badge
import social.plasma.ui.components.ProgressIndicator
import social.plasma.ui.components.StatCard
import social.plasma.ui.components.ZoomableAvatar
import social.plasma.ui.notes.GetOpenGraphMetadata
import social.plasma.ui.notes.NoteElevatedCard
import social.plasma.ui.profile.ProfileUiState.Loaded.ProfileStat
import social.plasma.ui.profile.ProfileUiState.Loaded.UserData
import social.plasma.ui.theme.PlasmaTheme
import timber.log.Timber

@Composable
fun Profile(
    uiState: ProfileUiState,
    modifier: Modifier = Modifier,
    onNoteDisposed: (NoteId) -> Unit,
    onNoteDisplayed: (NoteId) -> Unit,
    onNavigateBack: () -> Unit,
    onNoteClick: (NoteId) -> Unit,
    onNoteReaction: (NoteId) -> Unit,
    onRepostClick: (NoteId) -> Unit,
    onReply: (NoteId) -> Unit,
    getOpenGraphMetadata: GetOpenGraphMetadata,
    onNavigateToProfile: (PubKey) -> Unit,
) {
    when (uiState) {
        is ProfileUiState.Loading -> ProgressIndicator(modifier)
        is ProfileUiState.Loaded -> ProfileContent(
            uiState = uiState,
            onNoteDisplayed = onNoteDisplayed,
            onNoteDisposed = onNoteDisposed,
            modifier = modifier,
            onNavigateBack = onNavigateBack,
            onNoteClick = onNoteClick,
            onNoteReaction = onNoteReaction,
            onReply = onReply,
            getOpenGraphMetadata = getOpenGraphMetadata,
            onProfileClick = onNavigateToProfile,
            onRepostClick = onRepostClick
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ProfileContent(
    uiState: ProfileUiState.Loaded,
    onNoteDisplayed: (NoteId) -> Unit,
    onNoteDisposed: (NoteId) -> Unit,
    onNavigateBack: () -> Unit,
    onNoteClick: (NoteId) -> Unit,
    onRepostClick: (NoteId) -> Unit,
    onNoteReaction: (NoteId) -> Unit,
    onReply: (NoteId) -> Unit,
    onProfileClick: (PubKey) -> Unit,
    getOpenGraphMetadata: GetOpenGraphMetadata,
    modifier: Modifier = Modifier,
) {
    val lazyPagingItems = uiState.userNotesPagingFlow.collectAsLazyPagingItems()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
        contentWindowInsets = WindowInsets(left = 0, right = 0, top = 0, bottom = 0),
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues)
        ) {
            item {
                ProfileAppBar(
                    onNavigateBack = onNavigateBack,
                    userData = uiState.userData,
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                ProfileBio(userData = uiState.userData,
                    validateNip5 = uiState.isNip5Valid,
                    following = uiState.following,
                    onFollowClick = {
                        scope.launch {
                            // TODO implement following/unfollowing
                            snackbarHostState.showSnackbar(
                                "Coming Soon", duration = SnackbarDuration.Short
                            )
                        }
                    })
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }

            item { ProfileStatsRow(uiState.statCards) }
            item { Spacer(modifier = Modifier.height(32.dp)) }

            if (lazyPagingItems.itemCount == 0) {
                item {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }
            }

            items(lazyPagingItems, key = { it.key }) { cardUiModel ->
                if (cardUiModel?.hidden == false) {
                    val noteId = NoteId(cardUiModel.id)

                    LaunchedEffect(Unit) {
                        onNoteDisplayed(noteId)
                    }
                    NoteElevatedCard(
                        uiModel = cardUiModel,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .clickable {
                                onNoteClick(noteId)
                            },
                        onAvatarClick = null,
                        onLikeClick = { onNoteReaction(noteId) },
                        onReplyClick = { onReply(noteId) },
                        getOpenGraphMetadata = getOpenGraphMetadata,
                        onProfileClick = onProfileClick,
                        onNoteClick = onNoteClick,
                        onRepostClick = { onRepostClick(noteId) },
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    DisposableEffect(Unit) {
                        onDispose {
                            onNoteDisposed(noteId)
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
    onNavigateBack: () -> Unit,
    userData: UserData,
) {
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
            model = userData.banner,
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
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .buttonOverlay()
                ) {
                    Icon(
                        Icons.Default.ChevronLeft,
                        stringResource(R.string.back)
                    )
                }
            },
            actions = {
                userData.lud?.let {
                    LightningButton(lud = it)
                    Spacer(modifier = Modifier.width(12.dp))
                }
                SharePubkeyButton(pubKey = userData.publicKey)
                Spacer(modifier = Modifier.width(16.dp))
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
                }, contentAlignment = Alignment.Center
        ) {
            ZoomableAvatar(
                imageUrl = userData.avatarUrl,
                size = 88.dp,
            )
        }
    }
}

@Composable
private fun SharePubkeyButton(
    pubKey: PubKey,
) {
    val clipboardManager = LocalClipboardManager.current

    IconButton(
        modifier = Modifier.buttonOverlay(),
        onClick = {
            clipboardManager.setText(AnnotatedString(pubKey.bech32))
        }
    ) {
        Icon(
            painterResource(R.drawable.ic_plasma_key),
            stringResource(R.string.copy_public_key_to_clipboard)
        )
    }
}

@Composable
private fun LightningButton(lud: String) {
    var walletRequiredDialogVisible by remember { mutableStateOf(false) }
    val currentContext = LocalContext.current

    IconButton(modifier = Modifier.buttonOverlay(), onClick = {
        try {
            currentContext.startActivity(
                Intent(
                    Intent.ACTION_VIEW, Uri.parse("lightning:$lud")
                )
            )
        } catch (e: ActivityNotFoundException) {
            walletRequiredDialogVisible = true
            Timber.w(e)
        }
    }) {
        Icon(
            painterResource(id = R.drawable.ic_plasma_lightning_bolt), null
        )
    }

    fun closeDialog() {
        walletRequiredDialogVisible = false
    }

    if (walletRequiredDialogVisible) {
        ConfirmationDialog(
            title = stringResource(R.string.wallet_required),
            subtitle = stringResource(R.string.install_a_lightning_wallet_and_fund_it_with_bitcoin_before_zapping_users),
            confirmLabel = stringResource(R.string.okay),
            onConfirm = ::closeDialog,
            onDismiss = ::closeDialog
        )
    }
}

private fun Modifier.buttonOverlay() =
    background(Color.Black.copy(alpha = 0.3f), shape = CircleShape)

@Composable
private fun ProfileBio(
    userData: UserData,
    validateNip5: suspend (PubKey, String?) -> Boolean,
    following: Boolean?,
    onFollowClick: () -> Unit,
) {
    val isNip5Valid by produceState(initialValue = false, userData.nip5Identifier) {
        value = validateNip5(userData.publicKey, userData.nip5Identifier)
    }

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
                onClick = onFollowClick,
                enabled = following != null,
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(MaterialTheme.colorScheme.primary))
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

        if (isNip5Valid) {
            userData.nip5Domain?.let {
                Spacer(modifier = Modifier.height(8.dp))

                Nip5Badge(identifier = userData.nip5Domain)
            }
        }

        userData.about?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it)
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
        Profile(uiState = uiState,
            onNoteDisposed = {},
            onNoteDisplayed = {},
            onNavigateBack = {},
            onNoteClick = {},
            onNoteReaction = {},
            onReply = {},
            getOpenGraphMetadata = { null },
            onNavigateToProfile = {},
            onRepostClick = {})
    }
}
