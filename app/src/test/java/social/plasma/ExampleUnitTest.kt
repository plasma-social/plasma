package social.plasma

import app.dexstr.relay.Spike
import okhttp3.OkHttpClient
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun openWebSocket() {
        val client = OkHttpClient()
        Spike(client).doIt()
        Thread.sleep(999_999)
    }
}