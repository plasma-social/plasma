package social.plasma.data.daos.fakes

import kotlinx.coroutines.flow.Flow
import social.plasma.data.daos.LastRequestDao
import social.plasma.models.LastRequestEntity
import social.plasma.models.Request

class FakeLastRequestDao : LastRequestDao() {
    override suspend fun lastRequest(request: Request, resourceId: String): LastRequestEntity? {
        return null
    }

    override fun observeLastRequest(
        request: Request,
        resourceId: String,
    ): Flow<LastRequestEntity?> {
        TODO("Not yet implemented")
    }

    override suspend fun upsert(entity: LastRequestEntity): Long {
        return 0
    }

    override suspend fun purgeRequests(requests: List<Request>) {
        TODO("Not yet implemented")
    }

}
