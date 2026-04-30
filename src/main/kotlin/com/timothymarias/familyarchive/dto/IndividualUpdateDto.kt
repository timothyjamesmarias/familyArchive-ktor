package com.timothymarias.familyarchive.dto

data class IndividualUpdateDto(
    val gedcomId: String? = null,
    val givenName: String? = null,
    val surname: String? = null,
    val sex: String? = null,
    val isTreeRoot: Boolean = false,
)
