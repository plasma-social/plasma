package social.plasma.opengraph

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.net.URL
import kotlin.coroutines.EmptyCoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
class OpenGraphParserTest {
    @Test
    fun `can parse from twitter`() = runTest {
        val metadata = ogParser().parse(URL("https://twitter.com/jack/status/20"))

        with(metadata!!) {
            assertThat(title).isEqualTo("jack on Twitter")
            assertThat(description).isEqualTo("“just setting up my twttr”")
            assertThat(image).isNotEmpty()
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

    @Test
    fun `can parse from tidal`() = runTest {
        val metadata = ogParser().parse(URL("https://tidal.com/track/62305984"))

        assertThat(metadata?.title).isNotNull()
        assertThat(metadata?.description).isNotNull()
        assertThat(metadata?.image).isNotNull()
        assertThat(metadata?.siteName).isEqualTo("Music on TIDAL")
    }

    private fun CoroutineScope.ogParser(): OpenGraphParser {
        return OpenGraphParser(RealDocumentProvider(coroutineContext), EmptyCoroutineContext)
    }
}
