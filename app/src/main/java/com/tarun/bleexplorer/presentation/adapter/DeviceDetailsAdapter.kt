package com.tarun.bleexplorer.presentation.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.tarun.bleexplorer.R
import com.tarun.bleexplorer.presentation.model.GattItem
import com.tarun.bleexplorer.databinding.ItemDeviceDetailsBinding

class DeviceDetailsAdapter(
    private val context: Context,
    private val onItemClick: (GattItem) -> Unit
) : RecyclerView.Adapter<DeviceDetailsAdapter.ViewHolder>() {

    private var gattItems: List<GattItem> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemDeviceDetailsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            gattItems[position].let { gattItem ->
                gattTitle.text = gattItem.title

                if (gattItem.isService) {
                    uuid.text = gattItem.serviceUuid.toString()
                    root.setBackgroundColor(ContextCompat.getColor(context, R.color.grey))

                } else {
                    uuid.text = gattItem.uuid.toString()
                    root.setOnClickListener {
                        onItemClick(gattItem)
                    }
                }

                properties.text = gattItem.properties
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(list: List<GattItem>) {
        gattItems = list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = gattItems.size

    inner class ViewHolder(val binding: ItemDeviceDetailsBinding) :
        RecyclerView.ViewHolder(binding.root)
}
