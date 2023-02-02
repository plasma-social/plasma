package social.plasma.db.converters

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import javax.inject.Inject

@ProvidedTypeConverter
class TagsTypeConverter @Inject constructor(
    moshi: Moshi,
) {
    private val tagListType =
        Types.newParameterizedType(List::class.java, List::class.java, String::class.java)

    private val tagListAdapter = moshi.adapter<List<List<String>>>(tagListType)

    @TypeConverter
    fun fromTagList(tags: List<List<String>>?): String? = tagListAdapter.toJson(tags)

    @TypeConverter
    fun toTagList(tagsJson: String?): List<List<String>>? =
        tagsJson?.let { tagListAdapter.fromJson(it) }
}