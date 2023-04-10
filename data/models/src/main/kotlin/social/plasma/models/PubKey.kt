import app.cash.nostrino.crypto.PubKey

// TODO - this has been moved into the next version of Nostrino's PubKey type
fun PubKey.shortBech32() = with(encoded().drop(5)) {
    "${take(8)}:${takeLast(8)}"
}
