package com.ocm.bracelet_machine_sdk.model

internal data class BaseResponse<T>(
        val flag: Boolean,
        val msg: String?,
        val data: T
)