package social.plasma.db.reactions

class FakeReactionDao : ReactionDao() {
    override suspend fun isNoteLiked(pubkey: String, noteId: String): Boolean {
        return false
    }

}
