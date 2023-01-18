package social.plasma.repository

import kotlinx.coroutines.flow.Flow
import social.plasma.db.reactions.ReactionDao
import social.plasma.db.reactions.ReactionEntity

class FakeReactionDao : ReactionDao() {
    override fun observeNoteReactionCount(noteId: String): Flow<Int> {
        TODO("Not yet implemented")
    }

    override fun insert(entity: ReactionEntity) {
        TODO("Not yet implemented")
    }
}
