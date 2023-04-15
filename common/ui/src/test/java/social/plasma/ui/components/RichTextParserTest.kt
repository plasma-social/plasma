package social.plasma.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import app.cash.nostrino.crypto.PubKey
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import social.plasma.models.NoteId
import social.plasma.models.NoteMention
import social.plasma.models.ProfileMention
import social.plasma.ui.components.richtext.RichTextParser
import social.plasma.ui.components.richtext.RichTextTag

@OptIn(ExperimentalCoroutinesApi::class)
class RichTextParserTest {
    private val parser = RichTextParser(linkColor = Color.Blue)

    @Test
    fun `note with multiline note mentions`() = runTest {
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
        runTest {
            val plainText =
                "Some text \n\nhttps://apidocs.imgur.com/\n\n#[0]\n#[1] \n#[2]"

            val keys = listOf(
                PubKey.parse("npub1mm90r3fkz436p4jgtkcqdy4uvelgx3xru6ej242ktx096flmmhjsfqrwg0"),
                PubKey.parse("npub1zvrwm4n0rk3hftwyzl8csjaulatuvwvk2c3kc8u89mssgq7qrvks5zvf63"),
                PubKey.parse("npub1jcgyf7tcshfkc48w40g2s3769h0uw79mnx73hspwcpdgy049rm5spf50ps"),
            )

            val mentions = keys.mapIndexed { i, key ->
                i to ProfileMention(text = "@note$i", pubkey = key)
            }.toMap()

            val result = parser.parse(plainText, mentions)

            assertThat(result.text)
                .isEqualTo("Some text \n\nhttps://apidocs.imgur.com/\n\n@note0\n@note1 \n@note2")
        }
    }

    @Test
    fun `note with characters after mention`() = runTest {
        val plainText =
            "Hey #[0], have you considered collaborating with #[1]?"

        val mentions = mapOf(
            0 to ProfileMention(
                "@joe",
                PubKey.parse("npub1mm90r3fkz436p4jgtkcqdy4uvelgx3xru6ej242ktx096flmmhjsfqrwg0")
            ),
            1 to ProfileMention(
                "@will",
                PubKey.parse("npub1jcgyf7tcshfkc48w40g2s3769h0uw79mnx73hspwcpdgy049rm5spf50ps")
            )
        )

        val result = parser.parse(plainText, mentions)

        assertThat(result.text).isEqualTo(
            "Hey @joe, have you considered collaborating with @will?"
        )
    }

    @Test
    fun `note with characters before mention`() = runTest {
        val plainText =
            "Best people to follow:#[0] and #[1]. #followme3000"

        val mentions = mapOf(
            0 to ProfileMention(
                "@joe",
                PubKey.parse("npub1felyu03mh8xdszx927cyvgyf9yp83feyyug4505aa8cma5t6g9vql86w95")
            ),
            1 to ProfileMention(
                "@will",
                PubKey.parse("npub1tjkc9jycaenqzdc3j3wkslmaj4ylv3dqzxzx0khz7h38f3vc6mls4ys9w3")
            )
        )

        val result = parser.parse(plainText, mentions)

        assertThat(result.text).isEqualTo(
            "Best people to follow:@joe and @will. #followme3000"
        )

        assertThat(
            result.getStringAnnotations(
                RichTextTag.HASHTAG,
                0,
                result.length
            )
        ).containsExactly(
            AnnotatedString.Range(
                item = "#followme3000",
                start = 38,
                end = 51,
                tag = RichTextTag.HASHTAG
            )
        )
    }
}
