package com.tarun.bleexplorer.presentation.receiver

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat

class BondStateReceiver(
    private val callback: BondStateCallback
) : BroadcastReceiver() {

    interface BondStateCallback {
        fun onBonding(device: BluetoothDevice)
        fun onBonded(device: BluetoothDevice)
        fun onBondFailed(device: BluetoothDevice)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
            val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                when (device?.bondState) {
                    BluetoothDevice.BOND_BONDING -> callback.onBonding(device)
                    BluetoothDevice.BOND_BONDED -> callback.onBonded(device)
                    BluetoothDevice.BOND_NONE -> callback.onBondFailed(device)
                }
            }

        }
    }
}