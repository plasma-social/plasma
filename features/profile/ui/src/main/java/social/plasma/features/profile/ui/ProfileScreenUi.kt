package social.plasma.features.profile.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import app.cash.nostrino.crypto.PubKey
import coil.compose.AsyncImage
import com.slack.circuit.overlay.ContentWithOverlays
import com.slack.circuit.overlay.LocalOverlayHost
import com.slack.circuit.runtime.ui.Ui
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import social.plasma.features.feeds.screens.eventfeed.EventFeedUiState
import social.plasma.features.feeds.ui.EventFeedUi
import social.plasma.features.profile.screens.ProfileUiEvent
import social.plasma.features.profile.screens.ProfileUiState
import social.plasma.features.profile.screens.ProfileUiState.Loaded
import social.plasma.features.profile.screens.ProfileUiState.Loading
import social.plasma.models.Nip5Status
import social.plasma.ui.R
import social.plasma.ui.components.ConfirmationDialog
import social.plasma.ui.components.Nip5Badge
import social.plasma.ui.components.OutlinedPrimaryButton
import social.plasma.ui.components.OverlayIconButton
import social.plasma.ui.components.PrimaryButton
import social.plasma.ui.components.ProgressIndicator
import social.plasma.ui.components.StatCard
import social.plasma.ui.components.ZoomableAvatar
import social.plasma.ui.components.withHapticFeedBack
import social.plasma.ui.overlays.getZapAmount
import social.plasma.ui.rememberStableCoroutineScope
import social.plasma.ui.theme.PlasmaTheme
import java.util.UUID
import javax.inject.Inject

class ProfileScreenUi @Inject constructor() : Ui<ProfileUiState> {
    @Composable
    override fun Content(state: ProfileUiState, modifier: Modifier) {
        when (state) {
            is Loading -> ProgressIndicator(modifier)
            is Loaded -> ProfileContent(uiState = state, modifier = modifier)
        }
    }

    @Composable
    private fun ProfileContent(
        uiState: Loaded,
        modifier: Modifier = Modifier,
    ) {
        val onEvent = uiState.onEvent
        val snackbarHostState = remember { SnackbarHostState() }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            contentWindowInsets = WindowInsets.navigationBars,
            modifier = modifier,
        ) { paddingValues ->
            EventFeedUi(
                state = uiState.feedState.copy(displayRefreshButton = false),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 8.dp),
                headerContent = {
                    item("appbar") {
                        ProfileAppBar(
                            onNavigateBack = { onEvent(ProfileUiEvent.OnNavigateBack) },
                            userData = uiState.userData,
                            onZap = { sats -> onEvent(ProfileUiEvent.OnZapProfile(sats)) },
                            showLightningIcon = uiState.showLightningIcon,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item("bio") {
                        ProfileBio(userData = uiState.userData,
                            following = uiState.following,
                            nip5Status = uiState.nip5Status,
                            onFollowClick = withHapticFeedBack {
                                onEvent(ProfileUiEvent.OnFollowButtonClicked)
                            })
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    item("stats") {
                        ProfileStatsRow(uiState.statCards)
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            ) { feedItem ->
                Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                    feedItem()
                }
            }

        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ProfileAppBar(
        onNavigateBack: () -> Unit,
        onZap: (Long) -> Unit,
        userData: Loaded.UserData,
        showLightningIcon: Boolean,
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
                ),
                navigationIcon = {
                    OverlayIconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                    ) {
                        Icon(
                            Icons.Default.ChevronLeft,
                            stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    if (showLightningIcon) {
                        LightningButton(onZap = onZap)
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
                    .border(4.dp, MaterialTheme.colorScheme.background, CircleShape)
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

        OverlayIconButton(
            onClick = withHapticFeedBack {
                clipboardManager.setText(AnnotatedString(pubKey.encoded()))
            }
        ) {
            Icon(
                painterResource(R.drawable.ic_plasma_key),
                stringResource(R.string.copy_public_key_to_clipboard)
            )
        }
    }

    @Composable
    private fun LightningButton(onZap: (Long) -> Unit) {
        var walletRequiredDialogVisible by remember { mutableStateOf(false) }

        val coroutineScope = rememberStableCoroutineScope()
        val overlayHost = LocalOverlayHost.current

        OverlayIconButton(onClick = {
            coroutineScope.launch {
                onZap(overlayHost.getZapAmount())
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

    @Composable
    private fun ProfileBio(
        userData: Loaded.UserData,
        nip5Status: Nip5Status,
        following: Boolean?,
        onFollowClick: () -> Unit,
    ) {

        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        userData.displayName,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    userData.username?.let {
                        Text(
                            it,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                AnimatedContent(targetState = following, label = "") { isFollowing ->
                    when (isFollowing) {
                        true -> {
                            OutlinedPrimaryButton(onClick = onFollowClick) {
                                Icon(painterResource(R.drawable.ic_plasma_follow), null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(id = R.string.unfollow))
                            }
                        }

                        false, null -> {
                            PrimaryButton(onClick = onFollowClick, enabled = following != null) {
                                Text(text = stringResource(id = R.string.follow))
                            }
                        }
                    }
                }

            }

            Nip5Badge(nip5Status, modifier = Modifier.padding(top = 8.dp))

            userData.about?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it)
            }
        }
    }

    @Composable
    private fun ProfileStatsRow(statCards: List<Loaded.ProfileStat>) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            statCards.forEachIndexed { index, (label, value) ->
                StatCard(modifier = Modifier.weight(1f), label = label, value = value)

                if (index != statCards.lastIndex) {
                    Spacer(modifier = Modifier.width(16.dp))
                }
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
        ContentWithOverlays {
            ProfileScreenUi().Content(state = uiState, modifier = Modifier)
        }
    }
}

class ProfilePreviewProvider : PreviewParameterProvider<ProfileUiState> {
    override val values: Sequence<ProfileUiState> = sequenceOf(
        createFakeProfile(),
        createFakeProfile(nip5 = null),
        createFakeProfile(username = null),
        createFakeProfile(username = null, nip5 = null),
        createFakeProfile(displayName = UUID.randomUUID().toString().repeat(10)),
        Loading,
    )

    private fun createFakeProfile(
        nip5: String? = "plasma.social",
        username: String? = "@satoshi",
        displayName: String = "Satoshi Nakamoto",
    ): ProfileUiState =
        Loaded(
            showLightningIcon = true,
            feedState = EventFeedUiState(
                items = emptyFlow(),
                refreshText = "Refresh",
                listState = LazyListState(),
                displayRefreshButton = false,
                screenProvider = { TODO() },
                onEvent = {}
            ),
            statCards = listOf(
                Loaded.ProfileStat(
                    label = "Followers",
                    value = "2M",
                ),
                Loaded.ProfileStat(
                    label = "Following",
                    value = "3.5k",
                ),
                Loaded.ProfileStat(
                    label = "Relays",
                    value = "11",
                ),
            ),
            userData = Loaded.UserData(
                nip5Identifier = nip5,
                displayName = displayName,
                username = username,
                publicKey = PubKey.parse("npub1jem3jmdve9h94snjkuf5egagk7uupgxtu0eru33mzyms8ctzlk9sjhk73a"),
                about = "Developer @ a peer-to-peer electronic cash system",
                avatarUrl = "https://api.dicebear.com/5.x/bottts/jpg",
                website = "https://cash.app",
                banner = "val nostrichImage =\n" +
                        "        \"https://s3-alpha-sig.figma.com/img/4a90/2d76/b5f9770952063fd97aa73441dbeef396?Expires=1675036800&Signature=isHUrgxr-OJjU4HHfA~wfa-GTLIq~FT83RxqEurf13bTXwLykd-aHhsMXuLhx2Zqs-g5hCj4jM3355ngZlcY9qcrcrTgwcAxZLbwAhpntHl499McE9BU7aO7jG7j~eMy0Z7a~p3lFCHuQsyO7ukKZsawWVkCNtPdl8E-IQ~yxMc~LAB6QSlQlEJV7hIwBAbWgOKDgQ6spq-UFeoOee5Po02JCGtZOEb9vlxzFrhBKdCxCh1PdrX0~9Qb8rEeLGzAFzhJeOKJ0RYwzHsiGYGWsc1Ad9nvgoCXY2FwwIrixsxh3Jy87BivV4XCibvTE7YHhXwTRY29D-0Yun95GsHWWw__&Key-Pair-Id=APKAQ4GOSFWCVNEHN3O4\""
            ),
            onEvent = {},
        )
}
