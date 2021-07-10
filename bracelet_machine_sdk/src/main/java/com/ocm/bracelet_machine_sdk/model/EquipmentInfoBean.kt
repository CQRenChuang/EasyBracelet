package com.ocm.bracelet_machine_sdk.model

internal data class EquipmentInfoBean(
    val brakemachine_no: String,
    val json_config: JsonConfig,
    val mac_addr: String,
    val shop_id: Int,
    val shop_name: String
)