package social.plasma.ui.profile

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import social.plasma.models.PubKey
import social.plasma.ui.notes.NoteUiModel

sealed interface ProfileUiState {
    object Loading : ProfileUiState
    data class Loaded(
        val userNotesPagingFlow: Flow<PagingData<NoteUiModel>>,
        val statCards: List<ProfileStat>,
        val userData: UserData,
        val following: Boolean? = false,
        val isNip5Valid: suspend (PubKey, String?) -> Boolean = { _, _ -> false },
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
            val nip5Identifier: String?,
            val nip5Domain: String? = null,
            val lud: String?,
        )
    }
}


