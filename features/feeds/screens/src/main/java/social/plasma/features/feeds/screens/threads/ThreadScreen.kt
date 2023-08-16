package social.plasma.features.feeds.screens.threads

import kotlinx.parcelize.Parcelize
import social.plasma.common.screens.StandaloneScreen
import social.plasma.models.NoteId

@Parcelize
data class ThreadScreen(
    val noteId: NoteId,
) : StandaloneScreen
