package com.tarun.bleexplorer.presentation.ui

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.tarun.bleexplorer.presentation.adapter.DeviceListAdapter
import com.tarun.bleexplorer.presentation.viewmodel.DeviceListViewModel
import com.tarun.bleexplorer.databinding.FragmentDeviceListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DeviceListFragment : Fragment() {

    companion object {
        private const val TAG = "DeviceListFragment"
    }

    private lateinit var binding: FragmentDeviceListBinding
    private lateinit var deviceListAdapter: DeviceListAdapter

    private val viewModel: DeviceListViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDeviceListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        deviceListAdapter = DeviceListAdapter(requireContext()) { device ->
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                when (device.bondState) {
                    BluetoothDevice.BOND_BONDED -> {
                        (requireActivity() as MainActivity).connectToDevice(device)
                        val action =
                            DeviceListFragmentDirections.actionDeviceListFragmentToDeviceDetailsFragment()
                        findNavController().navigate(action)
                    }

                    else -> {
                        bondDevice(device)
                    }
                }
            }
        }

        binding.rvDeviceList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = deviceListAdapter
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.deviceList.collectLatest {
                deviceListAdapter.submitList(it)
            }
        }
    }

    private fun bondDevice(device: BluetoothDevice) {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (device.bondState == BluetoothDevice.BOND_NONE || device.bondState == BluetoothDevice.BOND_BONDED) {
                device.createBond()
                Log.d(TAG, "Initiating bonding with ${device.name} - ${device.address}")
            } else if (device.bondState == BluetoothDevice.BOND_BONDING) {
                Log.d(TAG, "Device ${device.name} - ${device.address} is already bonding.")
            } else if (device.bondState == BluetoothDevice.BOND_BONDED) {
                Log.d(TAG, "Device ${device.name} - ${device.address} is already bonded.")
                // Optionally, you could proceed to connect here if bonding is required for your use case
            }
        } else {
            (requireActivity() as MainActivity).requestBlePermissions(arrayOf(Manifest.permission.BLUETOOTH_CONNECT))
        }
    }
}