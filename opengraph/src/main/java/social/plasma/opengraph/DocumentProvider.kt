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
                .followRedirects(true)
                .ignoreContentType(true)
                .referrer(REFERRER)
                .timeout(TIMEOUT)
                .userAgent(USER_AGENT)
                .get()
        } catch (exception: Exception) {
            Timber.e(exception)
            null
        }
    }

    companion object {
        private const val TIMEOUT = 5_0000
        private const val REFERRER = "http://www.google.com"
        private const val USER_AGENT = "WhatsApp/2.19.81 A"
    }
}
