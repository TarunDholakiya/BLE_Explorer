package com.tarun.bleexplorer.presentation.adapter

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.tarun.bleexplorer.presentation.model.BondState
import com.tarun.bleexplorer.databinding.ItemDeviceBinding

class DeviceListAdapter(
    private val context: Context,
    private val onItemClick: (BluetoothDevice) -> Unit
) : RecyclerView.Adapter<DeviceListAdapter.ViewHolder>() {

    private var devices: List<BluetoothDevice> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = devices[position]

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        holder.binding.deviceNameTextView.text = device.name ?: "N/A"
        holder.binding.deviceAddressTextView.text = device.address
        holder.binding.deviceConnectionStatus.text = BondState.from(device.bondState).label

        holder.binding.root.setOnClickListener {
            onItemClick(device)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(list: List<BluetoothDevice>) {
        devices = list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = devices.size

    inner class ViewHolder(val binding: ItemDeviceBinding) : RecyclerView.ViewHolder(binding.root)
}
