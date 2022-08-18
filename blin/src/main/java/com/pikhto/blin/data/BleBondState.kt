package com.pikhto.blin.data

import android.bluetooth.BluetoothDevice
import com.pikhto.blin.BleBondManager
import java.util.*

data class BleBondState (val address: String, val state: BleBondManager.State) {
    constructor(bluetoothDevice: BluetoothDevice, state: BleBondManager.State) :
            this(bluetoothDevice.address, state)
}