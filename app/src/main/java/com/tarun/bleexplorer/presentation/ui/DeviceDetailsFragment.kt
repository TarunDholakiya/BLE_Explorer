package com.tarun.bleexplorer.presentation.ui

import android.Manifest
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tarun.bleexplorer.presentation.adapter.DeviceDetailsAdapter
import com.tarun.bleexplorer.presentation.viewmodel.DeviceDetailsViewModel
import com.tarun.bleexplorer.databinding.FragmentDeviceDetailsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.UUID

@AndroidEntryPoint
class DeviceDetailsFragment : Fragment() {

    private lateinit var binding: FragmentDeviceDetailsBinding
    private val viewModel: DeviceDetailsViewModel by activityViewModels()
    private lateinit var deviceDetailsAdapter: DeviceDetailsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDeviceDetailsBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        deviceDetailsAdapter = DeviceDetailsAdapter(requireContext()) { gattItem ->
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {


                val value = "Hello".toByteArray() // or hex, binary, etc.
                (requireActivity() as MainActivity).bluetoothGatt?.let {
                    /*writeToCharacteristic(
                        gatt = it,
                        characteristicUuid = gattItem.uuid,
                        serviceUuid = gattItem.serviceUuid,
                        valueToWrite = value
                    )*/


                    readCharacteristic(
                        gatt = it,
                        characteristicUuid = gattItem.uuid,
                        serviceUuid = gattItem.serviceUuid,
                    )
                }
            }
        }

        binding.rvDeviceDetailsList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = deviceDetailsAdapter
        }


        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.gattItems.collect {
                deviceDetailsAdapter.submitList(it)
            }
        }
    }

    private fun writeToCharacteristic(
        gatt: BluetoothGatt,
        characteristicUuid: UUID,
        serviceUuid: UUID,
        valueToWrite: ByteArray
    ) {
        val service = gatt.getService(serviceUuid)
        val characteristic = service?.getCharacteristic(characteristicUuid)

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (characteristic != null) {
                characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                characteristic.value = valueToWrite
                val result = gatt.writeCharacteristic(characteristic)
                Log.d("BLE", "Write result: $result")
            } else {
                Log.e("BLE", "Characteristic not found")
            }
        }
    }

    private fun readCharacteristic(
        gatt: BluetoothGatt,
        characteristicUuid: UUID,
        serviceUuid: UUID,
    ) {
        val service = gatt.getService(serviceUuid)
        val characteristic = service?.getCharacteristic(characteristicUuid)

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            gatt.readCharacteristic(characteristic)
        }
    }
}