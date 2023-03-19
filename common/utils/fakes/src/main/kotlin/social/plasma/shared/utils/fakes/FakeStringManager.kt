package social.plasma.shared.utils.fakes

import com.ibm.icu.text.MessageFormat
import social.plasma.shared.utils.api.StringManager


class FakeStringManager(
    vararg strings: Pair<Int, String>
) : StringManager {
    private val stringsMap = mapOf(*strings)

    override fun get(id: Int): String {
        return stringsMap[id] ?: throw IllegalArgumentException("Unexpected string id $id")
    }

    override fun getFormattedString(id: Int, args: Map<String, Any>): String {
        return MessageFormat.format(get(id), args)
    }
}
