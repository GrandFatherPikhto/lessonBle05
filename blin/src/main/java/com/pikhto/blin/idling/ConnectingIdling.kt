package com.pikhto.blin.idling

import androidx.test.espresso.IdlingResource
import com.pikhto.blin.orig.AbstractBleGattManager
import com.pikhto.blin.BleManagerInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.properties.Delegates

class ConnectingIdling(bleManager: BleManagerInterface) : IdlingResource {
    companion object {
        private var connectingIdling:ConnectingIdling? = null
        fun getInstance(bleManager: BleManagerInterface) : ConnectingIdling {
            return connectingIdling ?: ConnectingIdling(bleManager)
        }
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    private var resourceCallback: IdlingResource.ResourceCallback? = null

    private var isIdling = AtomicBoolean(true)

    var idling by Delegates.observable(false) { _, _, newState ->
        isIdling.set(newState)
        if (newState) {
            resourceCallback?.let { callback ->
                callback.onTransitionToIdle()
            }
        }
    }

    init {
        scope.launch {
            bleManager.stateFlowConnectState.collect { state ->
                when(state) {
                    AbstractBleGattManager.State.Connected -> {
                        idling = true
                    }
                    AbstractBleGattManager.State.Connecting -> {
                        idling = false
                    }
                    else -> { }
                }
            }
        }
    }

    override fun getName(): String = this.javaClass.simpleName

    override fun isIdleNow(): Boolean = isIdling.get()

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
        resourceCallback = callback
    }
}