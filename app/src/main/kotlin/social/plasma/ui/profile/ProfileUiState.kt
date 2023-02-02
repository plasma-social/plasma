package social.plasma.ui.profile

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import social.plasma.PubKey
import social.plasma.ui.components.NoteUiModel

sealed interface ProfileUiState {
    object Loading : ProfileUiState
    data class Loaded(
        val userNotesPagingFlow: Flow<PagingData<NoteUiModel>>,
        val statCards: List<ProfileStat>,
        val userData: UserData,
        val following: Boolean? = false,
    ) : ProfileUiState {
        data class ProfileStat(
            val label: String,
            val value: String,
        )

        data class UserData(
            val banner: String,
            val website: String?,
            val petName: String,
            val username: String?,
            val publicKey: PubKey,
            val about: String?,
            val avatarUrl: String,
            val nip5: String?,
        )
    }
}


