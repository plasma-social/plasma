package social.plasma.models

enum class Request(val tag: String) {
    SYNC_METADATA("sync_metadata"),
    SYNC_THREAD("sync_thread"),
    SYNC_HASHTAG("sync_hashtag"),
    VIEW_HASHTAG("view_hashtag"),
}
