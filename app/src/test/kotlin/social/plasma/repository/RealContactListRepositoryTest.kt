package social.plasma.repository

// TODO test with fake relays
//class RealContactListRepositoryTest : StringSpec({
//
//    val repo = RealContactListRepository(
//        Relays(
//            client,
//            scarlet,
//            listOf("wss://brb.io"),
//        ),
//        eventRefiner = EventRefiner(moshi)
//    )
//
//    "repository can be used to find user data" {
//        repo.observeContactLists(JemPubKey).filterNot { it.isEmpty() }.take(1).collect { set ->
//            set.map { contact -> contact.pubKey } shouldContain JackPubKey.decodeHex()
//        }
//    }
//})
