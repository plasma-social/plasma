package social.plasma.utils

import androidx.compose.ui.graphics.Color
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import social.plasma.models.NoteId
import social.plasma.models.PubKey
import social.plasma.ui.components.notes.RichTextParser
import social.plasma.ui.components.richtext.NoteMention
import social.plasma.ui.components.richtext.ProfileMention

class RichTextParserTest {
    private val parser = RichTextParser(linkColor = Color.Blue)

    @Test
    fun `note with multiline note mentions`() {
        val plainText =
            "Some text \n\nhttps://apidocs.imgur.com/\n\n#[0]\n#[1] \n#[2] \n#[3] \n#[4]"

        val mentions = (0..4).associateWith {
            NoteMention(text = "@note$it", noteId = NoteId("$it"))
        }

        val result = parser.parse(plainText, mentions)

        assertThat(result.text)
            .isEqualTo("Some text \n\nhttps://apidocs.imgur.com/\n\n@note0\n@note1 \n@note2 \n@note3 \n@note4")
    }

    @Test
    fun `note with multiline profile mentions`() {
        val plainText =
            "Some text \n\nhttps://apidocs.imgur.com/\n\n#[0]\n#[1] \n#[2] \n#[3] \n#[4]"

        val mentions = (0..4).associateWith {
            ProfileMention(text = "@note$it", pubkey = PubKey("note$it"))
        }

        val result = parser.parse(plainText, mentions)

        assertThat(result.text)
            .isEqualTo("Some text \n\nhttps://apidocs.imgur.com/\n\n@note0\n@note1 \n@note2 \n@note3 \n@note4")
    }

    @Test
    fun `note with characters after mention`() {
        val plainText =
            "Hey #[0], have you considered collaborating with #[1]?"

        val mentions = mapOf(
            0 to ProfileMention("@joe", PubKey("joekey")),
            1 to ProfileMention("@will", PubKey("Will"))
        )

        val result = parser.parse(plainText, mentions)

        assertThat(result.text).isEqualTo(
            "Hey @joe, have you considered collaborating with @will?"
        )
    }

    @Test
    fun `note with characters before mention`() {
        val plainText =
            "Best people to follow:#[0] and #[1]."

        val mentions = mapOf(
            0 to ProfileMention("@joe", PubKey("joekey")),
            1 to ProfileMention("@will", PubKey("Will"))
        )

        val result = parser.parse(plainText, mentions)

        assertThat(result.text).isEqualTo(
            "Best people to follow:@joe and @will."
        )
    }
}
