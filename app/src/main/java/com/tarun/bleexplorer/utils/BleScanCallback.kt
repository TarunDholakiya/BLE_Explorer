package com.tarun.bleexplorer.utils

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult

class BleScanCallback(
    private val onDeviceDiscovered: (BluetoothDevice) -> Unit,
    private val onScanError: (Int) -> Unit
) : ScanCallback() {

    override fun onScanResult(callbackType: Int, result: ScanResult?) {
        result?.device?.let { device ->
            onDeviceDiscovered(device)
        }
    }

    override fun onScanFailed(errorCode: Int) {
        onScanError(errorCode)
    }
}
