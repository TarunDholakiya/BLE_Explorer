package com.tarun.bleexplorer.utils

object GattNames {

    val serviceNames = mapOf(
        "00001800-0000-1000-8000-00805f9b34fb" to "Generic Access",
        "00001801-0000-1000-8000-00805f9b34fb" to "Generic Attribute",
        "0000180a-0000-1000-8000-00805f9b34fb" to "Device Information",
        "0000180f-0000-1000-8000-00805f9b34fb" to "Battery Service",
        "00001805-0000-1000-8000-00805f9b34fb" to "Current Time Service",
        "9fa480e0-4967-4542-9390-d343dc5d04ae" to "Fast Pair Service",
        "7905f431-b5ce-4e99-a40f-4b1e122d00d0" to "Apple Notification Center Service",
        "89d3502b-0f36-433a-8ef4-c502ad55f8dc" to "Microsoft Swift Pair Service",
        "d0611e78-bbb4-4591-a5f8-487910ae4366" to "Nordic UART Service"
    )

    val characteristicNames = mapOf(
        "00002a00-0000-1000-8000-00805f9b34fb" to "Device Name",
        "00002a01-0000-1000-8000-00805f9b34fb" to "Appearance",
        "00002a05-0000-1000-8000-00805f9b34fb" to "Service Changed",
        "00002a19-0000-1000-8000-00805f9b34fb" to "Battery Level",
        "00002a29-0000-1000-8000-00805f9b34fb" to "Manufacturer Name String",
        "00002a24-0000-1000-8000-00805f9b34fb" to "Model Number String",
        "00002a2b-0000-1000-8000-00805f9b34fb" to "Current Time",
        "00002a0f-0000-1000-8000-00805f9b34fb" to "Local Time Information",
        "af0badb1-5b99-43cd-917a-a77bc549e3cc" to "Fast Pair Key-Based Pairing",
        "8667556c-9a37-4c91-84ed-54ee27d90049" to "Nordic UART TX/RX",
        "69d1d8f3-45e1-49a8-9821-9bbdfdaad9d9" to "ANCS Notification Source",
        "9fbf120d-6301-42d9-8c58-25e699a21dbd" to "ANCS Control Point",
        "22eac6e9-24d6-4bb5-be44-b36ace7c7bfb" to "ANCS Data Source",
        "9b3c81d8-57b1-4a8a-b8df-0e56f7ca51c2" to "Microsoft Swift Pair",
        "c6b2f38c-23ab-46d8-a6ab-a3a870bbd5d7" to "Custom Device Info"
    )

    fun getServiceName(uuid: String): String {
        return serviceNames[uuid.lowercase()] ?: "Unknown Service"
    }

    fun getCharacteristicName(uuid: String): String {
        return characteristicNames[uuid.lowercase()] ?: "Characteristic: $uuid"
    }
}
