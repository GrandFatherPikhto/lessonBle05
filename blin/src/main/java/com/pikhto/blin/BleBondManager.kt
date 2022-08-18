package com.pikhto.blin

import android.content.Context
import com.pikhto.blin.orig.AbstractBleBondManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class BleBondManager(context: Context, dispatcher: CoroutineDispatcher = Dispatchers.IO)
    : AbstractBleBondManager(context, dispatcher)