package social.plasma.opengraph

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.net.URL

@OptIn(ExperimentalCoroutinesApi::class)
class OpenGraphParserTest {
    @Test
    fun `can parse from twitter without image`() = runTest {
        val metadata = ogParser().parse(URL("https://twitter.com/jack/status/20"))

        with(metadata!!) {
            assertThat(title).isEqualTo("jack on Twitter")
            assertThat(description).isEqualTo("“just setting up my twttr”")
            assertThat(image).isNull()
            assertThat(siteName).isEqualTo("Twitter")
        }
    }

    @Test
    fun `can parse from youtube`() = runTest {
        val metadata =
            ogParser().parse(URL("https://www.youtube.com/watch?v=enwbjGyD7VU"))

        assertThat(metadata?.title).isNotNull()
        assertThat(metadata?.description).isNotNull()
        assertThat(metadata?.image).isNotNull()
        assertThat(metadata?.siteName).isEqualTo("YouTube")
    }

    private fun CoroutineScope.ogParser(): OpenGraphParser {
        return OpenGraphParser(RealDocumentProvider(coroutineContext))
    }
}
