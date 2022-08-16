package com.pikhto.blin.data

import com.pikhto.blin.BleBondManager

data class BleBondState (val bleDevice: BleDevice, val state: BleBondManager.State)