package social.plasma.opengraph

import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import timber.log.Timber
import java.net.URL
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext

interface DocumentProvider {
    suspend fun get(url: URL): Document?
}

internal class RealDocumentProvider @Inject constructor(
    @Named("io") private val ioDispatcher: CoroutineContext,
) : DocumentProvider {
    override suspend fun get(url: URL): Document? = withContext(ioDispatcher) {
        return@withContext try {
            Jsoup.connect(url.toString())
                .userAgent(USER_AGENT)
                .ignoreContentType(true)
                .followRedirects(true)
                .get()
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    companion object {
        private const val USER_AGENT = "facebookexternalhit/1.1"
    }
}
