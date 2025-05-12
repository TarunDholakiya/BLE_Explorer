package com.tarun.bleexplorer.presentation.model

import java.util.UUID

data class GattItem(
    val title: String,
    val serviceUuid : UUID,
    val uuid: UUID,
    val isService: Boolean,
    val properties: String? = null
)
