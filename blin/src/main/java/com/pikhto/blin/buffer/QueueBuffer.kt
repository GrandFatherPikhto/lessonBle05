package com.pikhto.blin.buffer

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.util.Log
import com.pikhto.blin.data.BleGattItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlin.properties.Delegates

class QueueBuffer (dispatcher: CoroutineDispatcher = Dispatchers.IO) {
    private val tagLog = this.javaClass.simpleName
    private val scope = CoroutineScope(dispatcher)
    private val buffer = MutableListQueue<BleGattItem>()
    private val bufferMutex = Mutex(locked = false)

    var bluetoothGatt:BluetoothGatt? by Delegates.observable(null) { _, _, newValue ->
        newValue?.let { _ ->
            buffer.peek()?.let { nextGattData ->
                nextGattData(nextGattData)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun nextCharacteristic(bleGattItem: BleGattItem) : Boolean {
        bluetoothGatt?.let { gatt ->
            bleGattItem.getCharacteristic(gatt)?.let { characteristic ->
                when (bleGattItem.type) {
                    BleGattItem.Type.Write -> {
                        characteristic.value = bleGattItem.value
                        return gatt.writeCharacteristic(characteristic)
                    }
                    BleGattItem.Type.Read -> {
                        return gatt.readCharacteristic(characteristic)
                    }
                }

            }
        }

        return false
    }

    @SuppressLint("MissingPermission")
    private fun nextDescriptor(bleGattItem: BleGattItem) : Boolean {
        bluetoothGatt?.let { gatt ->
            bleGattItem.getDescriptor(gatt)?.let { descriptor ->
                when(bleGattItem.type) {
                    BleGattItem.Type.Write -> {
                        descriptor.value = bleGattItem.value
                        return gatt.writeDescriptor(descriptor)
                    }
                    BleGattItem.Type.Read -> {
                        return gatt.readDescriptor(descriptor)
                    }
                }
            }
        }

        return false
    }


    private fun nextGattData(bleGattData: BleGattItem) : Boolean {
        return if (bleGattData.uuidDescriptor == null) {
            nextCharacteristic(bleGattData)
        } else {
            nextDescriptor(bleGattData)
        }
    }

    private fun nextBufferGattData(bleGattData: BleGattItem) {
        Log.d(tagLog, "nextBufferGattData($bleGattData, ${buffer.peek()})")
        if (buffer.peek() == bleGattData) {
            buffer.dequeue()
            buffer.peek()?.let { nextGattData ->
                nextGattData(nextGattData)
            }
        }
    }

    fun onCharacteristicWrite (gatt: BluetoothGatt?,
                               characteristic: BluetoothGattCharacteristic?,
                               status: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            if (gatt != null && characteristic != null) {
                nextBufferGattData(BleGattItem(characteristic, BleGattItem.Type.Write))
            }
        } else {
            if (gatt != null && characteristic != null) {
                nextBufferGattData(BleGattItem(characteristic, BleGattItem.Type.Write))
            }
        }
    }

    fun onDescriptorWrite (gatt: BluetoothGatt?,
                           descriptor: BluetoothGattDescriptor?,
                           status: Int?) {

        if (status == BluetoothGatt.GATT_SUCCESS) {
            if (gatt != null && descriptor != null) {
                nextBufferGattData(BleGattItem(descriptor, BleGattItem.Type.Write))
            }
        } else {
            if (gatt != null && descriptor != null) {
                nextGattData(BleGattItem(descriptor, BleGattItem.Type.Write))
            }
        }
    }

    fun onCharacteristicRead (
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            if (gatt != null && characteristic != null) {
                nextBufferGattData(BleGattItem(characteristic, BleGattItem.Type.Read))
            }
        } else {
            if (gatt != null && characteristic != null) {
                nextCharacteristic(BleGattItem(characteristic, BleGattItem.Type.Read))
            }
        }
    }

    fun onDescriptorRead (
        gatt: BluetoothGatt?,
        descriptor: BluetoothGattDescriptor?,
        status: Int
    ) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            if (gatt != null && descriptor != null) {
                nextBufferGattData(BleGattItem(descriptor, BleGattItem.Type.Read))
            }
        } else {
            if (gatt != null && descriptor != null) {
                nextCharacteristic(BleGattItem(descriptor, BleGattItem.Type.Read))
            }
        }
    }

    fun addGattData(bleGattItem: BleGattItem) {
        Log.d(tagLog, "addGattData($bleGattItem)")
        buffer.enqueue(bleGattItem)
        if (buffer.count == 1) {
            nextGattData(bleGattItem)
        }
    }
}