package com.pikhto.blin

import com.pikhto.blin.orig.AbstractBleGattCallback
import com.pikhto.blin.orig.AbstractBleGattManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class BleGattCallback (bleGattManager: AbstractBleGattManager,
                       dispatcher: CoroutineDispatcher = Dispatchers.IO) :
    AbstractBleGattCallback(bleGattManager, dispatcher)