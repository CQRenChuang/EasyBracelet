package com.ocm.bracelet_machine_sdk.model

internal data class WirteReadSet(
    val block: String,
    val content: String,
    val opt_type: String,//1 清柜  2水控充值
    val psw: String,
    val sector: String
)