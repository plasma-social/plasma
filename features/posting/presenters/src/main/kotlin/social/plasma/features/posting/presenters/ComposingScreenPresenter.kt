package social.plasma.features.posting.presenters

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import app.cash.nostrino.crypto.PubKey
import com.slack.circuit.Navigator
import com.slack.circuit.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okio.ByteString.Companion.decodeHex
import okio.ByteString.Companion.toByteString
import social.plasma.domain.InvokeError
import social.plasma.domain.InvokeStatus
import social.plasma.domain.InvokeSuccess
import social.plasma.domain.interactors.GetHashtagSuggestions
import social.plasma.domain.interactors.GetNip5Status
import social.plasma.domain.interactors.GetUserTagSuggestions
import social.plasma.domain.interactors.SendNote
import social.plasma.domain.observers.ObserveUserMetadata
import social.plasma.features.posting.screens.AutoCompleteSuggestion
import social.plasma.features.posting.screens.AutoCompleteSuggestion.UserSuggestion
import social.plasma.features.posting.screens.ComposePostUiEvent
import social.plasma.features.posting.screens.ComposePostUiState
import social.plasma.features.posting.screens.ComposingScreen
import social.plasma.models.NoteWithUser
import social.plasma.models.ProfileMention
import social.plasma.models.TagSuggestion
import social.plasma.shared.repositories.api.AccountStateRepository
import social.plasma.shared.repositories.api.NoteRepository
import social.plasma.shared.utils.api.StringManager

class ComposingScreenPresenter @AssistedInject constructor(
    private val stringManager: StringManager,
    private val sendNote: SendNote,
    private val noteRepository: NoteRepository,
    private val getUserTagSuggestions: GetUserTagSuggestions,
    private val getHashtagSuggestions: GetHashtagSuggestions,
    private val getNip5Status: GetNip5Status,
    accountStateRepository: AccountStateRepository,
    observeMyMetadata: ObserveUserMetadata,
    @Assisted private val args: ComposingScreen,
    @Assisted private val navigator: Navigator,
) : Presenter<ComposePostUiState> {

    private val myPubKey = PubKey(accountStateRepository.getPublicKey()!!.toByteString())
    private val avatarUrlFlow = observeMyMetadata.flow.map { it?.picture }
    private val isReply = args.parentNote != null

    init {
        observeMyMetadata(ObserveUserMetadata.Params(myPubKey))
    }

    @Composable
    override fun present(): ComposePostUiState {
        var submitting by remember { mutableStateOf(false) }
        var noteContent by remember { mutableStateOf(TextFieldValue()) }
        val mentions = remember { mutableStateMapOf<String, ProfileMention>() }

        val rootNote by produceState<NoteWithUser?>(initialValue = null) {
            args.parentNote?.let { noteId ->
                value = noteRepository.getById(noteId)
            }
        }

        val buttonEnabled by produceState(false, noteContent, submitting, rootNote) {
            value = if (!isReply) {
                noteContent.text.isNotBlank() && !submitting
            } else {
                noteContent.text.isNotBlank() && !submitting && rootNote != null
            }
        }

        val avatarUrl by avatarUrlFlow.collectAsState(initial = null)

        val noteSubmitStatus by produceState<InvokeStatus?>(
            null,
            submitting,
            noteContent,
            rootNote
        ) {
            if (submitting) {
                value = sendNote.executeSync(
                    SendNote.Params(
                        content = noteContent.text,
                        parentNote = rootNote
                    )
                )
            }
        }

        val title by produceState(
            initialValue = if (isReply) "Replying" else stringManager[R.string.new_note],
            rootNote
        ) {
            rootNote?.let { note ->
                value =
                    "Replying to ${note.userMetadataEntity?.userFacingName ?: note.noteEntity.pubkey.decodeHex()}"
            }
        }

        val suggestedUsers by produceState<List<TagSuggestion>>(
            initialValue = emptyList(),
            noteContent
        ) {
            val cursorPosition =
                if (noteContent.selection.collapsed) noteContent.selection.start else 0

            value =
                getUserTagSuggestions.executeSync(
                    GetUserTagSuggestions.Params(
                        noteContent.text,
                        cursorPosition
                    )
                )
        }

        val suggestedHashTags by produceState<List<String>>(emptyList(), noteContent) {
            val cursorPosition =
                if (noteContent.selection.collapsed) noteContent.selection.start else 0

            value = getHashtagSuggestions.executeSync(
                GetHashtagSuggestions.Params(
                    noteContent.text,
                    cursorPosition
                )
            )
        }

        val autoCompleteSuggestions by produceState<List<AutoCompleteSuggestion>>(
            emptyList(),
            suggestedUsers,
            suggestedHashTags,
        ) {

            if (suggestedHashTags.isNotEmpty()) {
                value = suggestedHashTags.map {
                    AutoCompleteSuggestion.HashtagSuggestion(
                        hashTag = it,
                    )
                }
                return@produceState
            }

            if (suggestedUsers.isEmpty()) {
                value = emptyList()
                return@produceState
            }


            val list = suggestedUsers.map {
                UserSuggestion(
                    tagSuggestion = it,
                    nip5Valid = if (it.nip5Identifier == null) false else null
                )
            }.toMutableStateList()

            suggestedUsers.forEachIndexed { index, suggestion ->
                val nip5Identifier = suggestion.nip5Identifier ?: return@forEachIndexed
                if (nip5Identifier.trim().isEmpty()) return@forEachIndexed

                launch {
                    val nip5Status = getNip5Status.executeSync(
                        GetNip5Status.Params(
                            suggestion.pubKey,
                            nip5Identifier
                        )
                    ).isValid()

                    list[index] = UserSuggestion(suggestion, nip5Status)
                }
            }

            value = list
        }

        LaunchedEffect(noteSubmitStatus) {
            when (noteSubmitStatus) {
                is InvokeError -> {
                    submitting = false
                }

                InvokeSuccess -> {
                    navigator.pop()
                }

                else -> {
                    // no op
                }
            }
        }

        return ComposePostUiState(
            title = title,
            placeholder = stringManager[R.string.your_message],
            postButtonLabel = stringManager[R.string.post],
            avatarUrl = avatarUrl,
            postButtonEnabled = buttonEnabled,
            showAutoComplete = autoCompleteSuggestions.isNotEmpty(),
            mentions = mentions,
            noteContent = noteContent,
            autoCompleteSuggestions = autoCompleteSuggestions,
        ) { event ->
            when (event) {
                ComposePostUiEvent.OnBackClick -> navigator.pop()
                is ComposePostUiEvent.OnNoteChange -> noteContent = event.content
                ComposePostUiEvent.OnSubmitPost -> {
                    submitting = true
                }

                is ComposePostUiEvent.OnUserSuggestionTapped -> {
                    val profileMention = ProfileMention(
                        text = "@${event.suggestion.title}",
                        pubkey = event.suggestion.pubKey,
                    )
                    val replacement = "@${profileMention.pubkey.npub}"

                    mentions[replacement] = profileMention

                    noteContent =
                        noteContent.replaceTextForCurrentMention("@", "$replacement ") // trailing space to force the cursor to start a new "word"
                }

                is ComposePostUiEvent.OnHashTagSuggestionTapped -> {
                    noteContent =
                        noteContent.replaceTextForCurrentMention("#", "${event.hashtag} ")
                }
            }
        }
    }

    private fun TextFieldValue.replaceTextForCurrentMention(
        mentionDelimiter: String,
        replacement: String,
    ): TextFieldValue {
        val cursorPosition = selection.end
        val contentBeforeCursor = text.substring(0, cursorPosition)
        val contentAfterCursor = text.substring(cursorPosition, text.length)

        val contentBeforeCurrentMention = contentBeforeCursor.substringBeforeLast(mentionDelimiter)

        val updatedText = buildString {
            append(contentBeforeCurrentMention)
            append(replacement)
            append(contentAfterCursor)
        }

        val newCursorPosition = contentBeforeCurrentMention.length + replacement.length

        return copy(
            text = updatedText,
            selection = TextRange(newCursorPosition)
        )
    }

    @AssistedFactory
    interface Factory {
        fun create(args: ComposingScreen, navigator: Navigator): ComposingScreenPresenter
    }
}



