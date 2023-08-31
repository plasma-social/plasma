package social.plasma.profile.presenters

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.paging.PagingConfig
import app.cash.nostrino.crypto.PubKey
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import okio.ByteString.Companion.decodeHex
import okio.ByteString.Companion.toByteString
import shortBech32
import social.plasma.common.screens.AndroidScreens.ShareLightningInvoiceScreen
import social.plasma.domain.interactors.EventCountInteractor
import social.plasma.domain.interactors.FollowPubkey
import social.plasma.domain.interactors.GetLightningInvoice
import social.plasma.domain.interactors.GetNip5Status
import social.plasma.domain.interactors.GetPubkeyFollowerCount
import social.plasma.domain.interactors.SyncProfileData
import social.plasma.domain.interactors.UnfollowPubkey
import social.plasma.domain.observers.ObserveFollowingCount
import social.plasma.domain.observers.ObservePagedProfileFeed
import social.plasma.domain.observers.ObserveRelayCount
import social.plasma.domain.observers.ObserveUserIsInContacts
import social.plasma.domain.observers.ObserveUserMetadata
import social.plasma.features.profile.screens.ProfileScreen
import social.plasma.features.profile.screens.ProfileUiEvent
import social.plasma.features.profile.screens.ProfileUiState
import social.plasma.features.profile.screens.ProfileUiState.Loaded.ProfileStat
import social.plasma.features.profile.screens.ProfileUiState.Loaded.UserData
import social.plasma.feeds.presenters.feed.FeedStateProducer
import social.plasma.models.BitcoinAmount
import social.plasma.models.Nip5Status
import social.plasma.shared.repositories.api.AccountStateRepository
import social.plasma.shared.utils.api.NumberFormatter

class ProfileScreenPresenter @AssistedInject constructor(
    private val observeFollowingCount: ObserveFollowingCount,
    private val observeRelayCount: ObserveRelayCount,
    private val observeUserMetadata: ObserveUserMetadata,
    private val observePagedProfileFeed: ObservePagedProfileFeed,
    private val syncProfileData: SyncProfileData,
    private val observeUserIsInContacts: ObserveUserIsInContacts,
    private val getPubkeyFollowerCount: GetPubkeyFollowerCount,
    private val getNip5Status: GetNip5Status,
    private val followPubkey: FollowPubkey,
    private val unFollowPubkey: UnfollowPubkey,
    private val numberFormatter: NumberFormatter,
    private val getLightningInvoice: GetLightningInvoice,
    accountStateRepository: AccountStateRepository,
    private val feedStateProducer: FeedStateProducer,
    @Assisted private val args: ProfileScreen,
    @Assisted private val navigator: Navigator,
) : Presenter<ProfileUiState> {
    private val myPubKey = PubKey(accountStateRepository.getPublicKey()?.toByteString()!!)
    private val pubKey = PubKey(args.pubKeyHex.decodeHex())

    private val metadataInitialState = UserData(
        displayName = args.pubKeyHex,
        username = null,
        about = null,
        nip5Identifier = null,
        avatarUrl = null,
        publicKey = pubKey,
        website = null,
        banner = "https://pbs.twimg.com/media/FnbPBoKWYAAc0-F?format=jpg&name=4096x4096",
    )

    private val pagingFlow = observePagedProfileFeed.flow.onStart {
        observePagedProfileFeed(
            ObservePagedProfileFeed.Params(
                pubKey = pubKey,
                pagingConfig = PagingConfig(
                    pageSize = 20,
                )
            )
        )
    }

    private val contactsCount = observeFollowingCount.flow.onStart {
        observeFollowingCount(pubKey)
    }.map { numberFormatter.format(it) }

    private val relayCountFlow = observeRelayCount.flow.onStart {
        observeRelayCount(pubKey)
    }.map { numberFormatter.format(it) }

    private val userIsInMyContacts = observeUserIsInContacts.flow.onStart {
        observeUserIsInContacts(
            ObserveUserIsInContacts.Params(
                ownerPubKey = myPubKey,
                contactPubKey = pubKey
            )
        )
    }


    @Composable
    override fun present(): ProfileUiState {
        val feedState = feedStateProducer(navigator, pagingFlow)

        LaunchedEffect(Unit) {
            launch { observeUserMetadata(ObserveUserMetadata.Params(pubKey)) }
            launch { syncProfileData.executeSync(SyncProfileData.Params(pubKey)) }
        }

        val metadata by remember { observeUserMetadata.flow }.collectAsState(initial = null)
        val followingCount by contactsCount.collectAsState(initial = "0")
        val relayCount by relayCountFlow.collectAsState(initial = "0")

        val followerCount by produceState(initialValue = "?") {
            val countResult = getPubkeyFollowerCount.executeSync(
                GetPubkeyFollowerCount.Params(pubKey)
            )
            value = when (countResult) {
                EventCountInteractor.Result.Failure -> value
                is EventCountInteractor.Result.Success -> numberFormatter.format(countResult.count)
            }
        }

        val isProfileInMyContacts by userIsInMyContacts.collectAsState(initial = null)

        var optimisticFollowState by remember { mutableStateOf<Boolean?>(null) }

        val isProfileFollowedByMe = remember(isProfileInMyContacts, optimisticFollowState) {
            optimisticFollowState ?: isProfileInMyContacts
        }

        val nip5Status by produceState<Nip5Status>(initialValue = Nip5Status.Missing, metadata) {
            val nip5Indentifier = metadata?.nip05?.takeIf { it.isNotBlank() }

            if (nip5Indentifier == null) {
                value = Nip5Status.Missing
            } else {
                value = Nip5Status.Set.Loading(nip5Indentifier)
                value =
                    getNip5Status.executeSync(
                        GetNip5Status.Params(
                            pubKey,
                            nip5Indentifier
                        )
                    )
            }
        }

        val userData by produceState(initialValue = metadataInitialState, metadata) {
            metadata?.let {
                value = UserData(
                    displayName = metadata!!.displayName ?: pubKey.shortBech32(),
                    banner = metadata!!.banner
                        ?: "https://pbs.twimg.com/media/FnbPBoKWYAAc0-F?format=jpg&name=4096x4096",
                    website = metadata!!.website,
                    username = metadata!!.name?.let { "@$it" },
                    about = metadata!!.about,
                    nip5Identifier = metadata!!.nip05,
                    nip5Domain = metadata!!.nip05,
                    avatarUrl = metadata!!.picture ?: "",
                    publicKey = pubKey,
                )
            }
        }

        val coroutineScope = rememberCoroutineScope()

        return ProfileUiState.Loaded(
            feedState = feedState,
            statCards = listOf(
                ProfileStat(
                    label = "Following",
                    value = followingCount,
                ),
                ProfileStat(
                    label = "Followers",
                    value = followerCount,
                ),
                ProfileStat(
                    label = "Relays",
                    value = relayCount,
                )
            ),
            following = isProfileFollowedByMe,
            nip5Status = nip5Status,
            userData = userData,
            showLightningIcon = metadata?.lud06 != null || metadata?.lud16 != null
        ) { event ->
            when (event) {
                ProfileUiEvent.OnNavigateBack -> navigator.pop()
                ProfileUiEvent.OnFollowButtonClicked -> {
                    if (isProfileFollowedByMe == true) {
                        optimisticFollowState = false
                        coroutineScope.launch {
                            unFollowPubkey.executeSync(UnfollowPubkey.Params(args.pubKeyHex))
                        }
                    } else {
                        optimisticFollowState = true
                        coroutineScope.launch {
                            followPubkey.executeSync(FollowPubkey.Params(args.pubKeyHex))
                        }
                    }
                }

                is ProfileUiEvent.OnZapProfile -> {
                    coroutineScope.launch {
                        val tipAddress = metadata?.tipAddress
                        tipAddress ?: return@launch // show error dialog

                        if (event.satsAmount <= 0) return@launch

                        getLightningInvoice.executeSync(
                            GetLightningInvoice.Params(
                                tipAddress = tipAddress,
                                amount = BitcoinAmount(sats = event.satsAmount),
                                recipient = pubKey,
                            )
                        ).onSuccess { response ->
                            navigator.goTo(ShareLightningInvoiceScreen(response.invoice))
                        }.onFailure {
                            // TODO show error dialog
                        }
                    }
                }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(args: ProfileScreen, navigator: Navigator): ProfileScreenPresenter
    }
}
