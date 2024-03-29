package social.plasma.features.posting.ui.composepost

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import app.cash.nostrino.crypto.PubKey
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import social.plasma.models.ProfileMention
import social.plasma.ui.components.richtext.RichTextTag


class MentionsVisualTransformationTest {
    private fun transformation(mentions: Map<String, ProfileMention> = emptyMap()): MentionsVisualTransformation =
        MentionsVisualTransformation(Color.White, mentions)

    @Test
    fun `when there's no npub, text is not transformed`() {
        val filter = AnnotatedString("a string that does not have any npubs")
        val result =
            transformation().filter(filter)

        assertThat(result.text).isEqualTo(filter)
    }

    @Test
    fun `when there's an npub at the beginning, text is transformed properly`() {
        val input = AnnotatedString("$npubString is finally here!")
        val transformedText = "@mark is finally here!"

        val result = transformation(
            mentions = mapOf(
                npubString to ProfileMention(
                    text = "@mark",
                    pubkey = pubKey,
                )
            )
        )

        with(result.filter(input)) {
            assertThat(text.text).isEqualTo(transformedText)
            assertThat(offsetMapping.originalToTransformed(0)).isEqualTo(0)
            assertThat(offsetMapping.originalToTransformed(input.length)).isEqualTo(transformedText.length)
            assertThat(offsetMapping.originalToTransformed(input.lastIndexOf(npubString))).isEqualTo(
                transformedText.lastIndexOf("@mark")
            )
        }
    }

    @Test
    fun `when there are multiple npubs, text is transformed properly`() {
        val input = AnnotatedString("Hey $npubString, $npubString is finally here!")
        val transformedText = "Hey @mark, @mark is finally here!"

        val result = transformation(
            mentions = mapOf(
                npubString to ProfileMention(
                    text = "@mark",
                    pubkey = pubKey,
                )
            )
        )

        with(result.filter(input)) {
            assertThat(text.text).isEqualTo(transformedText)
            assertThat(offsetMapping.originalToTransformed(0)).isEqualTo(0)
            assertThat(offsetMapping.originalToTransformed(input.length)).isEqualTo(transformedText.length)
            assertThat(offsetMapping.originalToTransformed(input.lastIndexOf(npubString))).isEqualTo(
                transformedText.lastIndexOf("@mark")
            )
        }
    }

    @Test
    fun `when text contains hashtag, add hashtag annotated string tags`() {
        val input = AnnotatedString("#1team Hey $npubString, $npubString is finally here #foodstr!")
        val transformedText = "#1team Hey @mark, @mark is finally here #foodstr!"

        val result = transformation(
            mentions = mapOf(
                npubString to ProfileMention(
                    text = "@mark",
                    pubkey = pubKey,
                )
            )
        )

        with(result.filter(input)) {
            assertThat(text.text).isEqualTo(transformedText)
            assertThat(
                text.getStringAnnotations(
                    RichTextTag.HASHTAG,
                    0,
                    text.length
                )
            ).containsExactly(
                AnnotatedString.Range(
                    item = "#1team",
                    start = 0,
                    end = 6,
                    tag = RichTextTag.HASHTAG
                ),
                AnnotatedString.Range(
                    item = "#foodstr",
                    start = 40,
                    end = 48,
                    tag = RichTextTag.HASHTAG
                )
            ).inOrder()
        }
    }

    companion object {
        val pubKey =
            PubKey.parse("npub1z2aauyjavy9kfau3jn4cq3u0uvadjhkngxzv0uzh0e3pfuexdjcql0pyy7")
        const val npubString = "@npub1z2aauyjavy9kfau3jn4cq3u0uvadjhkngxzv0uzh0e3pfuexdjcql0pyy7"
    }
}
