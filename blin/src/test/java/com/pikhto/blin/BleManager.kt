package com.pikhto.blin

import android.content.Context
import com.pikhto.blin.orig.AbstractBleManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class BleManager (context: Context, dispatcher: CoroutineDispatcher = Dispatchers.IO)
    : AbstractBleManager(context, dispatcher)