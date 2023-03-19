package social.plasma.models

enum class Request(val tag: String) {
    SYNC_METADATA("sync_metadata"),
    SYNC_THREAD("sync_thread")
}