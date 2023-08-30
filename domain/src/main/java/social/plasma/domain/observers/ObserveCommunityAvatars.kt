package social.plasma.domain.observers

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import social.plasma.data.daos.HashtagDao
import social.plasma.domain.SubjectInteractor
import social.plasma.models.HashTag
import javax.inject.Inject

class ObserveCommunityAvatars @Inject constructor(
    private val hashtagDao: HashtagDao,
) : SubjectInteractor<ObserveCommunityAvatars.Params, List<String>>() {
    data class Params(val limit: Int, val hashTag: HashTag)

    override fun createObservable(params: Params): Flow<List<String>> =
        hashtagDao.observeCommunityLatestPictures(params.hashTag.name, params.limit)
            .distinctUntilChanged()
}

