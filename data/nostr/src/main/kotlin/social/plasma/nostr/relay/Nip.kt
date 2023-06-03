package social.plasma.nostr.relay

sealed interface Nip {
    val number: Int

    object EventCount : Nip {
        override val number: Int = 45
    }
}
