package io.beatmaps.common.api

enum class UserSearchSort {
    RELEVANCE,
    BPM,
    DURATION,
    UPVOTES,
    DOWNVOTES,
    RATIO,
    MAPS,
    RANKED_MAPS,
    FIRST_UPLOAD,
    LAST_UPLOAD,
    MAP_AGE;

    companion object {
        fun fromString(str: String?) = try {
            UserSearchSort.valueOf(str ?: "")
        } catch (e: Exception) {
            null
        }
    }
}

enum class ApiOrder {
    DESC, ASC;

    fun invert() = when (this) {
        DESC -> ASC
        ASC -> DESC
    }

    companion object {
        fun fromString(str: String?) = try {
            ApiOrder.valueOf(str ?: "")
        } catch (e: Exception) {
            null
        }
    }
}
