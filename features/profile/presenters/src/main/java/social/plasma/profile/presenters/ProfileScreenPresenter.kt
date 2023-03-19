package social.plasma.profile.presenters

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.paging.PagingConfig
import com.slack.circuit.Navigator
import com.slack.circuit.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import social.plasma.domain.interactors.GetNip5Status
import social.plasma.domain.interactors.Nip5Status
import social.plasma.domain.interactors.SyncProfileData
import social.plasma.domain.observers.ObserveFollowingCount
import social.plasma.domain.observers.ObservePagedProfileFeed
import social.plasma.domain.observers.ObserveUserIsInContacts
import social.plasma.domain.observers.ObserveUserMetadata
import social.plasma.features.profile.screens.ProfileScreen
import social.plasma.features.profile.screens.ProfileUiEvent
import social.plasma.features.profile.screens.ProfileUiState
import social.plasma.features.profile.screens.ProfileUiState.Loaded.ProfileStat
import social.plasma.feeds.presenters.feed.FeedPresenter
import social.plasma.models.PubKey
import social.plasma.shared.repositories.api.AccountStateRepository

class ProfileScreenPresenter @AssistedInject constructor(
    private val observeFollowingCount: ObserveFollowingCount,
    private val observeUserMetadata: ObserveUserMetadata,
    private val observePagedProfileFeed: ObservePagedProfileFeed,
    private val syncProfileData: SyncProfileData,
    private val observeUserIsInContacts: ObserveUserIsInContacts,
    private val getNip5Status: GetNip5Status,
    accountStateRepository: AccountStateRepository,
    feedPresenterFactory: FeedPresenter.Factory,
    @Assisted private val args: ProfileScreen,
    @Assisted private val navigator: Navigator,
) : Presenter<ProfileUiState> {
    private val myPubKey = PubKey.of(accountStateRepository.getPublicKey()!!)
    private val pubKey = PubKey(args.pubKeyHex)

    private val metadataInitialState =
        ProfileUiState.Loaded.UserData(
            petName = args.pubKeyHex,
            username = null,
            about = null,
            nip5Identifier = null,
            avatarUrl = null,
            publicKey = pubKey,
            website = null,
            banner = "https://pbs.twimg.com/media/FnbPBoKWYAAc0-F?format=jpg&name=4096x4096",
            lud = null,
        )

    private val feedsPresenter =
        feedPresenterFactory.create(navigator, observePagedProfileFeed.flow.onStart {
            observePagedProfileFeed(
                ObservePagedProfileFeed.Params(
                    pubKey = pubKey, pagingConfig = PagingConfig(
                        pageSize = 20,
                    )
                )
            )
        })

    private val contactsCount = observeFollowingCount.flow.onStart {
        observeFollowingCount(pubKey)
    }

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
        LaunchedEffect(Unit) {
            launch { observeUserMetadata(ObserveUserMetadata.Params(pubKey)) }
            launch { syncProfileData.executeSync(SyncProfileData.Params(pubKey)) }
        }

        val metadata by remember { observeUserMetadata.flow }.collectAsState(initial = null)
        val followingCount by contactsCount.collectAsState(initial = 0)
        val isProfileFollowedByMe by userIsInMyContacts.collectAsState(initial = null)
        val isNip5Valid by produceState(initialValue = false, metadata) {
            val status = getNip5Status.executeSync(
                GetNip5Status.Params(
                    pubKey = pubKey,
                    identifier = metadata?.nip05
                )
            )
            value = status == Nip5Status.Valid
        }

        val userData by produceState(initialValue = metadataInitialState, metadata) {
            metadata?.let {
                value = ProfileUiState.Loaded.UserData(
                    petName = metadata!!.displayName ?: pubKey.shortBech32,
                    banner = metadata!!.banner
                        ?: "https://pbs.twimg.com/media/FnbPBoKWYAAc0-F?format=jpg&name=4096x4096",
                    website = metadata!!.website,
                    username = metadata!!.name?.let { "@$it" },
                    about = metadata!!.about,
                    nip5Identifier = metadata!!.nip05,
                    nip5Domain = metadata!!.nip05?.split("@")?.getOrNull(1),
                    avatarUrl = metadata!!.picture ?: "",
                    publicKey = pubKey,
                    lud = metadata!!.lud,
                )
            }
        }

        return ProfileUiState.Loaded(
            feedState = feedsPresenter.present(),
            statCards = listOf(
                ProfileStat(
                    label = "Following",
                    value = "$followingCount"
                ),
                ProfileStat(
                    label = "Followers",
                    value = "💜"
                ),
                ProfileStat(
                    label = "Relays",
                    value = "11"
                )
            ),
            following = isProfileFollowedByMe,
            isNip5Valid = isNip5Valid,
            userData = userData,
        ) { event ->
            when (event) {
                ProfileUiEvent.OnNavigateBack -> navigator.pop()
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(args: ProfileScreen, navigator: Navigator): ProfileScreenPresenter
    }
}