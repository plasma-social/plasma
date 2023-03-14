package social.plasma.ui.util

import java.time.Instant

interface InstantFormatter {
    /**
     * Formats an instant into a human readable format.
     */
    fun getRelativeTime(instant: Instant): String
}
