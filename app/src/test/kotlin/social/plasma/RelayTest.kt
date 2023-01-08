package social.plasma

import social.plasma.relay.Relays
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Test

class RelayTest {

    @Test
    fun openWebSocket() = runBlocking {
        val relays = Relays(OkHttpClient())
        val flow = relays.subscribe("wss://relay.damus.io")
        val messages = flow.take(3).toList()
        messages.forEach { println(it) }
        assert(messages.size == 3)
    }
}