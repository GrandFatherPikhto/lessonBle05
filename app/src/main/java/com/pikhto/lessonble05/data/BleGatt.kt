package com.pikhto.lessonble05.data

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattService

data class BleGatt(val bleDevice: BleDevice, val services: List<BluetoothGattService>) {
    constructor(bluetoothGatt: BluetoothGatt) : this (
                BleDevice(bluetoothGatt.device),
                bluetoothGatt.services
            )
    val device get() = bleDevice
}
