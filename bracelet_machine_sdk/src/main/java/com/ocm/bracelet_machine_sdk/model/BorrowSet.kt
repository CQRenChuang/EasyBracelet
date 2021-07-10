package com.ocm.bracelet_machine_sdk.model

internal data class BorrowSet(
    val read_set: List<WirteReadSet>,
    val write_set: List<WirteReadSet>
)