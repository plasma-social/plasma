package social.plasma.features.posting.ui.composepost

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import social.plasma.models.ProfileMention
import social.plasma.models.PubKey
import social.plasma.models.crypto.Bech32.bechToBytes


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

    companion object {
        val pubKey =
            PubKey.of("npub1z2aauyjavy9kfau3jn4cq3u0uvadjhkngxzv0uzh0e3pfuexdjcql0pyy7".bechToBytes())
        const val npubString = "@npub1z2aauyjavy9kfau3jn4cq3u0uvadjhkngxzv0uzh0e3pfuexdjcql0pyy7"
    }
}
