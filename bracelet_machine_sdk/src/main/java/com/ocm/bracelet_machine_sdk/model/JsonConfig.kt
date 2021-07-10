package com.ocm.bracelet_machine_sdk.model

internal data class JsonConfig(
    val back_from: Int,
    val back_set: BackSet,
    val borrow_set: BorrowSet,
    val card_type: String,
    val tips: String,
    val user_crowd: Int
)