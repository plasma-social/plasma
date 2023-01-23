package social.plasma.repository

// TODO test with fake relays

//class RealUserMetaDataRepositoryTest : StringSpec({
//
//    val userMetadataDao = FakeUserMetadataDao()
//    val repo = RealUserMetaDataRepository(
//        Relays(client, scarlet, listOf("wss://brb.io")),
//        userMetadataDao,
//        EventRefiner(
//            moshi
//        ),
//        ioDispatcher = StandardTestDispatcher(),
//    )
//
//    "repository can be used to find user data" {
//        // TODO - have this return subscriptions that can be used to unsubscribe
//        repo.requestUserMetaData(JemPubKey)
//        repo.observeUserMetaData(JemPubKey).take(1).collect {
//            it.pubkey shouldBe JemPubKey
//        }
//    }
//})
