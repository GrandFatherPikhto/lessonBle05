package com.pikhto.lessonble05.blemanager

import android.content.Context
import com.pikhto.blin.BleManager
import com.pikhto.blin.data.BleGatt
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class AppBleManager(context: Context, dispatcher: CoroutineDispatcher = Dispatchers.IO) :
    BleManager(context) {

    private val mutableStateFlowBleGatt = MutableStateFlow<BleGatt?>(null)
    val stateFlowBleGatt = mutableStateFlowBleGatt.asStateFlow()
    val bleGatt get() = mutableStateFlowBleGatt.value

    init {
        scope.launch {
            stateFlowBluetoothGatt.collect { bluetoothGatt ->
                if (bluetoothGatt == null) {
                    mutableStateFlowBleGatt.tryEmit(null)
                } else {
                    mutableStateFlowBleGatt.tryEmit(BleGatt(bluetoothGatt))
                }
            }
        }
    }
}