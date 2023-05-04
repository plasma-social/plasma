package social.plasma.opengraph

import androidx.collection.LruCache
import kotlinx.coroutines.withContext
import org.jsoup.nodes.Document
import java.net.URL
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class OpenGraphParser @Inject constructor(
    private val documentProvider: DocumentProvider,
    @Named("default") private val defaultDispatcher: CoroutineContext,
) {
    private val lruCache = LruCache<String, OpenGraphMetadata>(100)

    suspend fun parse(url: URL): OpenGraphMetadata? = withContext(defaultDispatcher) {
        val cached = lruCache[url.toString()]

        if (cached != null) {
            return@withContext cached
        }

        val doc = documentProvider.get(url)

        doc ?: return@withContext null

        val metadata = getOpenGraphMetadata(doc, url)

        lruCache.put(url.toString(), metadata)

        return@withContext metadata
    }

    private fun getOpenGraphMetadata(
        doc: Document,
        url: URL,
    ): OpenGraphMetadata {
        val ogTags = doc.head().select(OG_TAG_SELECTOR)

        var metadata = OpenGraphMetadata()

        for (tag in ogTags) {
            when (tag.attr(ATTR_PROPERTY)) {
                TITLE -> metadata = metadata.copy(title = tag.attr(ATTR_CONTENT))
                DESCRIPTION -> metadata = metadata.copy(description = tag.attr(ATTR_CONTENT))
                IMAGE -> metadata = metadata.copy(image = tag.attr(ATTR_CONTENT))
                SITE_NAME -> metadata = metadata.copy(siteName = tag.attr(ATTR_CONTENT))
            }
        }

        if (metadata.title == null) {
            metadata = metadata.copy(title = doc.title())
        }

        if (metadata.siteName == null) {
            metadata = metadata.copy(siteName = url.authority)
        }
        return metadata
    }

    companion object {
        private const val OG_TAG_SELECTOR = "meta[property^=og:]"
        private const val ATTR_PROPERTY = "property"
        private const val ATTR_CONTENT = "content"

        private const val TITLE = "og:title"
        private const val IMAGE = "og:image"
        private const val DESCRIPTION = "og:description"
        private const val SITE_NAME = "og:site_name"
    }
}
