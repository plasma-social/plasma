package social.plasma.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Duration
import java.time.Instant


@Entity(
    tableName = "last_requests",
    indices = [Index(value = ["request", "resource_id"], unique = true)],
)
data class LastRequestEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "request") val request: Request,
    @ColumnInfo(name = "resource_id") val resourceId: String,
) {
    fun isStillValid(timeout: Duration): Boolean {
        val timeToLive = Instant.now().minus(timeout)

        return timestamp.isAfter(timeToLive)
    }

    @ColumnInfo(name = "timestamp")
    internal var _timestamp: Long = Instant.now().epochSecond


    @delegate:Ignore
    val timestamp: Instant by lazy { Instant.ofEpochSecond(_timestamp) }
}