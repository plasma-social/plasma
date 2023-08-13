package social.plasma.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EventModel(
    val id: String,
    val pubkey: String,
    val createdAt: Long,
    val kind: Int,
    val tags: List<List<String>>,
    val content: String,
    val sig: String,
) : Parcelable
