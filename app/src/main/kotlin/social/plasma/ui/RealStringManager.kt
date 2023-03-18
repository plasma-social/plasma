package social.plasma.ui

import android.content.Context
import com.ibm.icu.text.MessageFormat
import javax.inject.Inject

// TODO move to a common module
class RealStringManager @Inject constructor(
    private val context: Context
) : StringManager {
    override fun get(id: Int): String {
        return context.getString(id)
    }

    override fun getFormattedString(id: Int, args: Map<String, Any>) : String {
        return MessageFormat.format(get(id), args)
    }
}