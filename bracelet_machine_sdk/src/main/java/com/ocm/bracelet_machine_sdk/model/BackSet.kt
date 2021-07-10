package com.ocm.bracelet_machine_sdk.model

internal data class BackSet(
    val read_set: List<WirteReadSet>,
    val write_set: List<WirteReadSet>
)