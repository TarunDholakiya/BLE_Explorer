package com.tarun.bleexplorer.utils

import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.widget.Toast

object Utils {
    fun Context.showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Helper extension function to easily get characteristic properties as a string
    fun BluetoothGattCharacteristic.propertiesToString(): String {
        val properties = mutableListOf<String>()
        if (this.properties and BluetoothGattCharacteristic.PROPERTY_READ != 0) properties.add("Read")
        if (this.properties and BluetoothGattCharacteristic.PROPERTY_WRITE != 0) properties.add("Write")
        if (this.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE != 0) properties.add(
            "WriteNoResponse"
        )
        if (this.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0) properties.add("Notify")
        if (this.properties and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0) properties.add("Indicate")
        return properties.joinToString(", ")
    }
}