package com.tarun.bleexplorer.presentation.viewmodel

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class DeviceListViewModel @Inject constructor() : ViewModel() {

    private val _deviceList = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val deviceList: StateFlow<List<BluetoothDevice>> = _deviceList.asStateFlow()

    fun onDeviceDiscovered(newDevice: BluetoothDevice) {
        val currentList = _deviceList.value

        val isNew = currentList.none { it.address == newDevice.address }
        if (isNew) {
            _deviceList.value = currentList + newDevice
        }
    }

    fun clearScannedDevices() {
        _deviceList.value = emptyList()
    }

}