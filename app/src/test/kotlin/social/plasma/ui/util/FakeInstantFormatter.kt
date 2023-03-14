package social.plasma.ui.util

import kotlinx.coroutines.flow.MutableStateFlow
import java.time.Instant

class FakeInstantFormatter : InstantFormatter {
    val formatResponse = MutableStateFlow("now")

    override fun getRelativeTime(instant: Instant): String {
        return formatResponse.value
    }

}
