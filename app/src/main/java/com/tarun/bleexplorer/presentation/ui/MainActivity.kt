package com.tarun.bleexplorer.presentation.ui

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.tarun.bleexplorer.utils.BleScanCallback
import com.tarun.bleexplorer.presentation.receiver.BondStateReceiver
import com.tarun.bleexplorer.presentation.viewmodel.DeviceDetailsViewModel
import com.tarun.bleexplorer.presentation.viewmodel.DeviceListViewModel
import com.tarun.bleexplorer.R
import com.tarun.bleexplorer.utils.Utils.propertiesToString
import com.tarun.bleexplorer.utils.Utils.showToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), BondStateReceiver.BondStateCallback {

    companion object {
        private const val TAG = "MainActivity"
    }

    private val deviceListViewModel: DeviceListViewModel by viewModels()
    private val deviceDetailsViewModel: DeviceDetailsViewModel by viewModels()

    private lateinit var bondStateReceiver: BondStateReceiver
    private lateinit var scanCallback: ScanCallback

    var bluetoothGatt: BluetoothGatt? = null

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val isBluetoothEnabled: Boolean
        get() = bluetoothAdapter?.isEnabled == true

    private val scannedDevices = mutableListOf<BluetoothDevice>()

    private lateinit var enableBluetoothLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    private val bluetoothLeScanner by lazy { bluetoothAdapter?.bluetoothLeScanner }

    private val scanFilters =
        emptyList<android.bluetooth.le.ScanFilter>() // You can add filters later

    private var isScanning = false


    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        registerBluetoothLauncher()
        registerPermissionLauncher()
        checkBluetoothAndPermissions()

        setupScanCallback()
        bondStateReceiver = BondStateReceiver(this)
        registerReceiver(bondStateReceiver, IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED))


    }

    private fun setupScanCallback() {
        scanCallback = BleScanCallback(
            onDeviceDiscovered = { device ->
                val isNewDevice = scannedDevices.none { it.address == device.address }
                if (isNewDevice) {
                    scannedDevices.add(device)
                    deviceListViewModel.onDeviceDiscovered(device)
                }
            },
            onScanError = { errorCode ->
                showToast(getString(R.string.scan_failed_with_error, errorCode))
            }
        )
    }


    private fun registerPermissionLauncher() {
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                if (permissions.all { it.value }) {
                    startBleScan()
                } else {
                    this.showToast(getString(R.string.permissions_not_granted))
                }
            }
    }


    private fun registerBluetoothLauncher() {
        enableBluetoothLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    startBleScan()
                } else {
                    this.showToast(getString(R.string.bluetooth_enabling_was_cancelled))
                }
            }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onBonding(device: BluetoothDevice) {
        showToast(getString(R.string.bonding_started_with, device.name, device.address))
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onBonded(device: BluetoothDevice) {
        showToast(getString(R.string.bonded_with, device.name, device.address))
        connectToDevice(device)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onBondFailed(device: BluetoothDevice) {
        showToast(getString(R.string.bonding_failed_or_removed_for, device.name, device.address))
    }


    private fun checkBluetoothAndPermissions() {
        when {
            packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE).not() -> {
                showToast(getString(R.string.ble_not_supported_on_this_device))
                finish()
            }

            isBluetoothEnabled.not() -> {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                enableBluetoothLauncher.launch(enableBtIntent)
            }

            else -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    requestBlePermissions(
                        arrayOf(
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    )
                }
            }
        }
    }


    fun requestBlePermissions(permissions: Array<String>) {
        if (permissions.any {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }) {
            permissionLauncher.launch(permissions)
        } else {
            startBleScan()
        }
    }


    private fun startBleScan() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        if (isScanning.not()) {
            val scanSettings = android.bluetooth.le.ScanSettings.Builder()
                .setScanMode(android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()
            scannedDevices.clear()
            deviceListViewModel.clearScannedDevices()
            bluetoothLeScanner?.startScan(scanFilters, scanSettings, scanCallback)
            isScanning = true
            Log.d(TAG, "BLE scan started")
        }
    }


    // Function to stop BLE scan (to be implemented later)
    private fun stopBleScan() {
        Log.d(TAG, "stopBleScan()")
    }


    override fun onPause() {
        super.onPause()
        stopBleScan() // Stop scanning when the activity is paused to save battery
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        registerReceiver(bondStateReceiver, filter)
    }

    override fun onStop() {
        super.onStop()
        disconnectGattServer()
        unregisterReceiver(bondStateReceiver)
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.i(TAG, "Connected to GATT server.")
                    gatt?.discoverServices() // Initiate service discovery
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.i(TAG, "Disconnected from GATT server.")
                    // Handle disconnection UI updates and cleanup
                }

                else -> {
                    Log.w(TAG, "Connection state changed to: $newState with status $status")
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "Services discovered.")
                gatt?.services?.let { deviceDetailsViewModel.processGattServices(services = it) }
                gatt?.services?.forEach { service ->
                    Log.i(TAG, "Service UUID: ${service.uuid}")
                    service.characteristics?.forEach { characteristic ->
                        Log.i(
                            TAG,
                            "  Characteristic UUID: ${characteristic.uuid}, Properties: ${characteristic.propertiesToString()}"
                        )
                    }
                }
            } else {
                Log.w(TAG, "onServicesDiscovered received: $status")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, value, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(
                    TAG,
                    "Characteristic read: ${characteristic.uuid} - Value: ${value.contentToString()}"
                )
                val charValue = characteristic.value?.toString(Charsets.UTF_8) ?: "Empty"
                runOnUiThread(kotlinx.coroutines.Runnable {
                    showToast("Value: $charValue")
                })
            } else {
                Log.w(
                    TAG,
                    "Characteristic read failed for ${characteristic?.uuid}, status: $status"
                )
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            // Called when a write operation on a characteristic completes
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "Wrote to characteristic ${characteristic?.uuid}")
            } else {
                Log.w(
                    TAG,
                    "Failed to write characteristic: ${characteristic?.uuid}, status: $status"
                )
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            // Called when the remote device sends a notification or indication
            val value = characteristic?.value
            if (value != null) {
                Log.i(
                    TAG,
                    "Notification/Indication received for ${characteristic.uuid}: ${value.contentToString()} (raw)"
                )
                // Parse the 'value' byte array
            }
        }

        override fun onDescriptorRead(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int,
            value: ByteArray // Note the nullable ByteArray
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(
                    TAG,
                    "Descriptor read: ${descriptor.uuid} - Value: ${value.contentToString()}"
                )
            } else {
                Log.w(TAG, "Descriptor read failed for ${descriptor.uuid}, status: $status")
            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS && descriptor != null) {
                Log.i(
                    TAG,
                    "Successfully wrote descriptor: ${descriptor.uuid} for characteristic: ${descriptor.characteristic.uuid}"
                )
            } else if (status == BluetoothGatt.GATT_FAILURE && descriptor != null) {
                Log.w(
                    TAG,
                    "Failed to write descriptor: ${descriptor.uuid} for characteristic: ${descriptor.characteristic.uuid}, status: $status"
                )
            } else if (descriptor != null) {
                Log.i(TAG, "Wrote to descriptor: ${descriptor.uuid}, status: $status")
            }
        }

        override fun onReliableWriteCompleted(gatt: BluetoothGatt?, status: Int) {
            // Called when a reliable write transaction completes
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "Reliable write completed.")
            } else {
                Log.w(TAG, "Reliable write failed: $status")
            }
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
            // Called when the RSSI (signal strength) of the remote device is read
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "Read RSSI: $rssi")
                // Update UI with RSSI value
            } else {
                Log.w(TAG, "Failed to read RSSI: $status")
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            // Called when the MTU (Maximum Transmission Unit) changes
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "MTU changed to: $mtu")
            } else {
                Log.w(TAG, "Failed to change MTU: $status")
            }
        }

    }


    fun connectToDevice(device: BluetoothDevice) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            bluetoothGatt = device.connectGatt(this, false, gattCallback)
            Log.i(TAG, "Attempting to connect to ${device.name} - ${device.address}")
        } else {
            requestBlePermissions(arrayOf(Manifest.permission.BLUETOOTH_CONNECT))
        }
    }

    private fun disconnectGattServer() {
        if (bluetoothGatt != null) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                bluetoothGatt?.disconnect()
                bluetoothGatt?.close()
                bluetoothGatt = null
                Log.i(TAG, "Disconnected from GATT server")
                return
            }
        }
    }
}