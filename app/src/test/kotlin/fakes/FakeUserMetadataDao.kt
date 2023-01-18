package fakes

import app.cash.turbine.Turbine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import social.plasma.db.usermetadata.UserMetadataDao
import social.plasma.db.usermetadata.UserMetadataEntity
import kotlin.time.Duration.Companion.seconds

class FakeUserMetadataDao : UserMetadataDao {
    val inserts = Turbine<UserMetadataEntity>(timeout = 10.seconds)

    override fun observeUserMetadata(pubKey: String): Flow<UserMetadataEntity> {
        return inserts.asChannel().receiveAsFlow()
    }

    override fun insert(userMetadataEntity: UserMetadataEntity) {
        inserts.add(userMetadataEntity)
    }
}
