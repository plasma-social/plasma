package social.plasma.shared.utils.real

import com.ibm.icu.number.Notation
import social.plasma.shared.utils.api.NumberFormatter
import java.util.Locale
import javax.inject.Inject
import com.ibm.icu.number.NumberFormatter as IbmFormatter

class RealNumberFormatter @Inject constructor() : NumberFormatter {
    private val localizedNumberFormatter =
        IbmFormatter.with().notation(Notation.compactShort()).locale(Locale.getDefault())

    override fun format(number: Number): String {
        return localizedNumberFormatter.format(number).toString()
    }
}
