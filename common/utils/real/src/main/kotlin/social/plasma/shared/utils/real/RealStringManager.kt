package social.plasma.shared.utils.real

import android.content.Context
import com.ibm.icu.text.MessageFormat
import social.plasma.shared.utils.api.StringManager
import javax.inject.Inject

internal class RealStringManager @Inject constructor(
    private val context: Context
) : StringManager {
    override fun get(id: Int): String {
        return context.getString(id)
    }

    override fun getFormattedString(id: Int, args: Map<String, Any>) : String {
        return MessageFormat.format(get(id), args)
    }
}