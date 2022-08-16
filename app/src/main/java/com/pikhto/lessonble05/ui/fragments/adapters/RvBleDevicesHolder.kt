package com.pikhto.lessonble05.ui.fragments.adapters

import android.bluetooth.BluetoothDevice
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.pikhto.blin.data.BleScanResult
import com.pikhto.lessonble05.R
import com.pikhto.lessonble05.databinding.LayoutBleDeviceBinding

class RvBleDevicesHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val binding = LayoutBleDeviceBinding.bind(view)

    fun bind(bleScanResult: BleScanResult) {
        binding.apply {
            tvDeviceName.text =
                bleScanResult.device.name
                    ?: itemView.context.getString(R.string.unknown_device)
            tvDeviceAddress.text = bleScanResult.device.address
            tvRssi.text = itemView.context.getString(R.string.rssi_title,
                bleScanResult.rssi)
            if (bleScanResult.device.bondState == BluetoothDevice.BOND_BONDED) {
                ivBondState.setImageResource(R.drawable.ic_paired)
            } else {
                ivBondState.setImageResource(R.drawable.ic_unpaired)
            }
            if (bleScanResult.isConnectable) {
                ivConnectable.setImageResource(R.drawable.ic_connectable)
            } else {
                ivConnectable.setImageResource(R.drawable.ic_no_connectable)
            }
        }
    }
}