package social.plasma.ui

import com.ibm.icu.text.MessageFormat


// TODO move to a common module
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
