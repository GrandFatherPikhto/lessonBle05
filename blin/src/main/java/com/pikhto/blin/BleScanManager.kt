package com.pikhto.blin

import android.content.Context
import com.pikhto.blin.orig.AbstractBleScanManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class BleScanManager (context: Context, dispatcher: CoroutineDispatcher = Dispatchers.IO) :
    AbstractBleScanManager(context, dispatcher)