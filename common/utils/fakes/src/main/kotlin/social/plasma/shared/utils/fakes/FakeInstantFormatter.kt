package social.plasma.shared.utils.fakes

import social.plasma.shared.utils.api.InstantFormatter
import java.time.Instant

class FakeInstantFormatter : InstantFormatter {
    var formatResponse = ""

    override fun getRelativeTime(instant: Instant): String {
        return formatResponse
    }
}
