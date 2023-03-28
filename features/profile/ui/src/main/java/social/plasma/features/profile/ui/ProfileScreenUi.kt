package social.plasma.features.profile.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import coil.compose.AsyncImage
import com.slack.circuit.Ui
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import social.plasma.features.feeds.screens.feed.FeedUiState
import social.plasma.features.feeds.ui.FeedUi
import social.plasma.features.profile.screens.ProfileUiEvent.OnNavigateBack
import social.plasma.features.profile.screens.ProfileUiState
import social.plasma.features.profile.screens.ProfileUiState.Loaded
import social.plasma.features.profile.screens.ProfileUiState.Loading
import social.plasma.models.PubKey
import social.plasma.ui.R
import social.plasma.ui.components.ConfirmationDialog
import social.plasma.ui.components.Nip5Badge
import social.plasma.ui.components.OverlayIconButton
import social.plasma.ui.components.ProgressIndicator
import social.plasma.ui.components.StatCard
import social.plasma.ui.components.ZoomableAvatar
import social.plasma.ui.theme.PlasmaTheme
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

class ProfileScreenUi @Inject constructor(
    private val feedUi: FeedUi,
) : Ui<ProfileUiState> {
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
        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            modifier = modifier,
        ) { paddingValues ->
            feedUi.ListContent(
                state = uiState.feedState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 8.dp),
            ) {
                item("appbar") {
                    ProfileAppBar(
                        onNavigateBack = { onEvent(OnNavigateBack) },
                        userData = uiState.userData,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item("bio") {
                    ProfileBio(userData = uiState.userData,
                        isNip5Valid = uiState.isNip5Valid,
                        following = uiState.following,
                        onFollowClick = {
                            scope.launch {
                                // TODO implement following/unfollowing
                                snackbarHostState.showSnackbar(
                                    "Coming Soon", duration = SnackbarDuration.Short
                                )
                            }
                        })
                    Spacer(modifier = Modifier.height(32.dp))
                }

                item("stats") {
                    ProfileStatsRow(uiState.statCards)
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ProfileAppBar(
        onNavigateBack: () -> Unit,
        userData: Loaded.UserData,
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

        OverlayIconButton(
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

        OverlayIconButton(onClick = {
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

    @Composable
    private fun ProfileBio(
        userData: Loaded.UserData,
        isNip5Valid: Boolean,
        following: Boolean?,
        onFollowClick: () -> Unit,
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
                            it,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                OutlinedButton(
                    onClick = onFollowClick,
                    enabled = following != null,
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = SolidColor(
                            MaterialTheme.colorScheme.primary
                        )
                    )
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

                    Nip5Badge(identifier = it)
                }
            }

            userData.about?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it)
            }
        }
    }

    @Composable
    private fun ProfileStatsRow(statCards: List<Loaded.ProfileStat>) {
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

}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun PreviewProfile(
    @PreviewParameter(ProfilePreviewProvider::class) uiState: ProfileUiState,
) {
    PlasmaTheme {
        ProfileScreenUi(FeedUi()).Content(state = uiState, modifier = Modifier)
    }
}

class ProfilePreviewProvider : PreviewParameterProvider<ProfileUiState> {
    override val values: Sequence<ProfileUiState> = sequenceOf(
        createFakeProfile(),
        createFakeProfile(nip5 = null),
        createFakeProfile(username = null),
        createFakeProfile(username = null, nip5 = null),
        Loading,
    )

    private fun createFakeProfile(
        nip5: String? = "plasma.social",
        username: String? = "@satoshi",
    ): ProfileUiState =
        Loaded(
            feedState = FeedUiState(emptyFlow(), { null }, {}),
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
                petName = "Satoshi",
                username = username,
                publicKey = PubKey(UUID.randomUUID().toString()),
                about = "Developer @ a peer-to-peer electronic cash system",
                avatarUrl = "https://api.dicebear.com/5.x/bottts/jpg",
                website = "https://cash.app",
                lud = "lnurl",
                banner = "val nostrichImage =\n" +
                        "        \"https://s3-alpha-sig.figma.com/img/4a90/2d76/b5f9770952063fd97aa73441dbeef396?Expires=1675036800&Signature=isHUrgxr-OJjU4HHfA~wfa-GTLIq~FT83RxqEurf13bTXwLykd-aHhsMXuLhx2Zqs-g5hCj4jM3355ngZlcY9qcrcrTgwcAxZLbwAhpntHl499McE9BU7aO7jG7j~eMy0Z7a~p3lFCHuQsyO7ukKZsawWVkCNtPdl8E-IQ~yxMc~LAB6QSlQlEJV7hIwBAbWgOKDgQ6spq-UFeoOee5Po02JCGtZOEb9vlxzFrhBKdCxCh1PdrX0~9Qb8rEeLGzAFzhJeOKJ0RYwzHsiGYGWsc1Ad9nvgoCXY2FwwIrixsxh3Jy87BivV4XCibvTE7YHhXwTRY29D-0Yun95GsHWWw__&Key-Pair-Id=APKAQ4GOSFWCVNEHN3O4\""
            ),
            onEvent = {},
        )
}
