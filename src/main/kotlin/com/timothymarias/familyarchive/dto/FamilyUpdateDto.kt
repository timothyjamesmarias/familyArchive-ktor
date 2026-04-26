package com.timothymarias.familyarchive.dto

data class FamilyUpdateDto(
    val gedcomId: String = "",
    val marriageDateString: String? = null,
    val marriagePlaceId: Long? = null,
    val divorceDateString: String? = null,
)
