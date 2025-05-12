package com.tarun.bleexplorer.presentation.model

import android.bluetooth.BluetoothDevice

enum class BondState(val label: String) {
    BONDED("Bonded"),
    BONDING("Bonding"),
    NONE("Not Bonded");

    companion object {
        fun from(state: Int): BondState {
            return when (state) {
                BluetoothDevice.BOND_BONDED -> BONDED
                BluetoothDevice.BOND_BONDING -> BONDING
                BluetoothDevice.BOND_NONE -> NONE
                else -> NONE
            }
        }
    }
}