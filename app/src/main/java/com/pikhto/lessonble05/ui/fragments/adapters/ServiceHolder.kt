package com.pikhto.lessonble05.ui.fragments.adapters

import android.bluetooth.BluetoothGattService
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.pikhto.blin.GenericUUIDs.genericStringUUID
import com.pikhto.blin.helper.hasFlag
import com.pikhto.lessonble05.R
import com.pikhto.lessonble05.data.BleItem
import com.pikhto.lessonble05.databinding.LayoutServiceBinding
import com.pikhto.blin.GenericUUIDs
import com.pikhto.blin.GenericUUIDs.findGeneric

class ServiceHolder (private val view: View) : RecyclerView.ViewHolder(view) {

    private val binding = LayoutServiceBinding.bind(view)

    private val tagLog = this.javaClass.simpleName

    private var _bleItem:BleItem? = null
    private val bleItem get() = _bleItem!!

    private fun getString(resId: Int, vararg formatArgs: String) =
        view.context.getString(resId, formatArgs)

    private fun getTextServiceType() : String? =
        if (bleItem.serviceType.hasFlag(BluetoothGattService.SERVICE_TYPE_PRIMARY))
            getString(R.string.service_primary)
        else if (bleItem.serviceType.hasFlag(BluetoothGattService.SERVICE_TYPE_SECONDARY))
            getString(R.string.service_secondary)
        else null


    fun bind(item: BleItem) {
        _bleItem = item
        binding.apply {
            tvServiceName.text = bleItem.uuidService
                .findGeneric(type = GenericUUIDs.Type.Service)?.name
                    ?: getString(R.string.custom_service)
            tvServiceUuid.text = bleItem.uuidService
                .genericStringUUID()

            tvServiceType.text = getTextServiceType() ?: ""

            if (bleItem.opened) {
                ivUpDown.setImageResource(R.drawable.ic_up)
            } else {
                ivUpDown.setImageResource(R.drawable.ic_down)
            }
        }
    }
}