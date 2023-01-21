package social.plasma.ui.profile

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import social.plasma.models.PubKey
import social.plasma.ui.components.NoteCardUiModel

sealed interface ProfileUiState {
    object Loading : ProfileUiState
    data class Loaded(
        val userNotesPagingFlow: Flow<PagingData<NoteCardUiModel>>,
        val statCards: List<ProfileStat>,
        val userData: UserData,
        val following: Boolean? = false,
    ) : ProfileUiState {
        data class ProfileStat(
            val label: String,
            val value: String,
        )

        data class UserData(
            val petName: String,
            val username: String?,
            val publicKey: PubKey,
            val about: String?,
            val avatarUrl: String,
            val nip5: String?,
        )
    }
}


