package com.tarun.bleexplorer.presentation.viewmodel

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import androidx.lifecycle.ViewModel
import com.tarun.bleexplorer.presentation.model.GattItem
import com.tarun.bleexplorer.utils.GattNames
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class DeviceDetailsViewModel @Inject constructor() : ViewModel() {
    private val _gattItems = MutableStateFlow<List<GattItem>>(emptyList())
    val gattItems: StateFlow<List<GattItem>> = _gattItems.asStateFlow()

    fun processGattServices(services: List<BluetoothGattService>) {
        val flatList = services.flatMap { service ->
            val serviceUuid = service.uuid
            val serviceItem = GattItem(
                title = GattNames.getServiceName(serviceUuid.toString()),
                serviceUuid = serviceUuid,
                uuid = serviceUuid,
                isService = true
            )

            val charItems = service.characteristics.map { char ->
                GattItem(
                    title = GattNames.getCharacteristicName(char.uuid.toString()),
                    serviceUuid = serviceUuid,
                    uuid = char.uuid,
                    isService = false,
                    properties = buildPropertiesString(char.properties)
                )
            }

            listOf(serviceItem) + charItems
        }
        _gattItems.value = flatList
    }


    fun buildPropertiesString(props: Int): String {
        val propsList = mutableListOf<String>()
        if (props and BluetoothGattCharacteristic.PROPERTY_READ != 0) propsList.add("Read")
        if (props and BluetoothGattCharacteristic.PROPERTY_WRITE != 0) propsList.add("Write")
        if (props and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0) propsList.add("Notify")
        if (props and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0) propsList.add("Indicate")
        return propsList.joinToString()
    }
}