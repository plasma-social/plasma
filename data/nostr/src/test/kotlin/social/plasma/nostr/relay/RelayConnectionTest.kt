package social.plasma.nostr.relay

import io.kotest.core.spec.style.StringSpec
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.toByteString
import social.plasma.nostr.BuildingBlocks

class RelayConnectionTest : StringSpec({

//    val sec = run {
//        val bs = ByteArray(32)
//        SecureRandom().nextBytes(bs)
//        bs
//    }
//    val pub = Sign.publicKeyFromPrivate(BigInteger(sec.toByteString().hex()))

    val msgHash = "hi".toByteArray().toByteString().sha256().toByteArray()

    "can get events from a relay" {
        runTest {
            val relay = BuildingBlocks.testRelay(this)
            println("connecting")
            relay.connect()
            println("connected")
            /*
                relay.send(
                    EventMessage(
                        Event.createEvent(
                            pubKey = pair.public.toByteArray().toByteString(),
                            secretKey = pair.privateKey.toByteArray().toByteString(),
                            createdAt = Instant.now(),
                            kind = Event.Kind.Note,
                            tags = emptyList(),
                            content = "testing 1, 2, 3"
                        )
                    )
                )
        */
            println("disconnecting")
            relay.disconnect()
            println("done")
        }
    }

})
