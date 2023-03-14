package social.plasma.ui.util

import android.text.format.DateUtils
import java.time.Instant
import javax.inject.Inject

class RealInstantFormatter @Inject constructor() : InstantFormatter {
    override fun getRelativeTime(instant: Instant): String {
        return DateUtils.getRelativeTimeSpanString(
            instant.toEpochMilli(),
            Instant.now().toEpochMilli(),
            DateUtils.SECOND_IN_MILLIS,
            DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR or DateUtils.FORMAT_ABBREV_ALL
        ).toString()
    }
}
