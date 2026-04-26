package com.timothymarias.familyarchive.repository

data class PageRequest(
    val page: Int = 0,
    val size: Int = 20,
    val sortBy: String? = null,
    val sortDesc: Boolean = true,
)

data class Page<T>(
    val content: List<T>,
    val totalElements: Long,
    val number: Int,
    val size: Int,
) {
    val totalPages: Int get() = if (size > 0) ((totalElements + size - 1) / size).toInt() else 0
    val numberOfElements: Int get() = content.size
    val hasNext: Boolean get() = number < totalPages - 1
    val hasPrevious: Boolean get() = number > 0
    val isFirst: Boolean get() = number == 0
    val isLast: Boolean get() = number >= totalPages - 1
    val isEmpty: Boolean get() = content.isEmpty()
}
