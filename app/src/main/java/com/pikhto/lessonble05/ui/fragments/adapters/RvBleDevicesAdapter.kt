package com.pikhto.lessonble05.ui.fragments.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pikhto.lessonble05.R
import com.pikhto.lessonble05.data.BleScanResult
import com.pikhto.lessonble05.helper.OnClickItemListener
import com.pikhto.lessonble05.helper.OnLongClickItemListener

class RvBleDevicesAdapter : RecyclerView.Adapter<RvBleDevicesHolder> () {
    private val tagLog = javaClass.simpleName
    private val scanResults = mutableListOf<BleScanResult>()
    private var onClickItemListener: OnClickItemListener<BleScanResult>? = null
    private var onLongClickItemListener: OnLongClickItemListener<BleScanResult>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RvBleDevicesHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_ble_device, parent, false)
        return RvBleDevicesHolder(view)
    }

    override fun onBindViewHolder(holder: RvBleDevicesHolder, position: Int) {
        holder.itemView.setOnClickListener { view ->
            onClickItemListener?.let { listener ->
                listener(scanResults[position], view)
            }
        }
        holder.itemView.setOnLongClickListener { view ->
            onLongClickItemListener?.let { listener ->
                listener(scanResults[position], view)
            }
            true
        }
        holder.bind(scanResults[position])
    }

    override fun getItemCount(): Int = scanResults.size

    fun addScanResult(bleScanResult: BleScanResult) {
        if (scanResults.contains(bleScanResult)){
            val index = scanResults.indexOf(bleScanResult)
            if (index >= 0) {
                scanResults[index] = bleScanResult
                notifyItemChanged(index)
                Log.d(tagLog, "Update: $index ${bleScanResult.device.address}")
            }
        } else {
            scanResults.add(bleScanResult)
            notifyItemInserted(scanResults.indexOf(bleScanResult))
        }
    }

    fun setItemOnClickListener(onClickItemListener: OnClickItemListener<BleScanResult>) {
        this.onClickItemListener = onClickItemListener
    }

    fun setItemOnLongCliclListener(onLongClickItemListener: OnLongClickItemListener<BleScanResult>) {
        this.onLongClickItemListener = onLongClickItemListener
    }
}