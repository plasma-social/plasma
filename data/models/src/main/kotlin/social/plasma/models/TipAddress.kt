package social.plasma.models

sealed interface TipAddress {
    data class LightningAddress(val address: String) : TipAddress
    data class Lnurl(val bech32: String) : TipAddress
}
