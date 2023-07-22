package social.plasma.models

sealed interface TipAmount

data class BitcoinAmount(val sats: Long) : TipAmount {
    val millisats: Long
        get() = sats * 1000
}


