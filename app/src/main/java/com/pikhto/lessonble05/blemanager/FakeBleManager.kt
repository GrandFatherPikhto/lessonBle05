package com.pikhto.lessonble05.blemanager

import android.bluetooth.*
import android.content.Context
import android.util.Log
import com.pikhto.blin.BleGattManager
import com.pikhto.blin.BleManager
import com.pikhto.blin.BleManagerInterface
import com.pikhto.blin.BleScanManager
import com.pikhto.blin.buffer.BleCharacteristicNotify
import com.pikhto.blin.data.BleGattItem
import com.pikhto.blin.data.*
import com.pikhto.blin.idling.ConnectingIdling
import com.pikhto.blin.idling.DisconnectingIdling
import com.pikhto.blin.idling.ScanIdling
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*
import kotlin.random.Random

class FakeBleManager(context: Context, dispatcher: CoroutineDispatcher = Dispatchers.IO) : AppBleManager (context, dispatcher) {

    private val logTag = this.javaClass.simpleName

    private val mutableStateFlowScanState = MutableStateFlow(BleScanManager.State.Stopped)
    override val stateFlowScanState: StateFlow<BleScanManager.State>
        get() = mutableStateFlowScanState.asStateFlow()
    override val scanState: BleScanManager.State
        get() = mutableStateFlowScanState.value

    private val mutableScanResults = mutableListOf<BleScanResult>()
    private val mutableSharedFlowScanResult = MutableSharedFlow<BleScanResult>(replay = 100)
    override val sharedFlowBleScanResult: SharedFlow<BleScanResult>
        get() = mutableSharedFlowScanResult.asSharedFlow()
    override val scanResults: List<BleScanResult>
        get() = mutableScanResults.toList()

    private val mutableFlowScanError = MutableStateFlow(-1)
    override val stateFlowScanError: StateFlow<Int>
        get() = mutableFlowScanError.asStateFlow()
    override val scanError: Int
        get() = mutableFlowScanError.value

    private val mutableStateFlowConnectState = MutableStateFlow(BleGattManager.State.Disconnected)
    override val stateFlowConnectState: StateFlow<BleGattManager.State>
        get() = mutableStateFlowConnectState.asStateFlow()
    override val connectState: BleGattManager.State
        get() = mutableStateFlowConnectState.value

    private val mutableSharedFlowStateCode = MutableStateFlow(-1)
    override val sharedFlowConnectStateCode: SharedFlow<Int>
        get() = mutableSharedFlowStateCode.asStateFlow()

    private val mutableStateFlowBluetoothGatt = MutableStateFlow<BluetoothGatt?>(null)
    override val stateFlowBluetoothGatt: StateFlow<BluetoothGatt?>
        get() = mutableStateFlowBluetoothGatt.asStateFlow()
    override val bluetoothGatt: BluetoothGatt?
        get() = mutableStateFlowBluetoothGatt.value

    private val mutableSharedFlowCharacteristic = MutableSharedFlow<BluetoothGattCharacteristic>(replay = 100)
    override val sharedFlowCharacteristic: SharedFlow<BluetoothGattCharacteristic>
        get() = mutableSharedFlowCharacteristic.asSharedFlow()

    private val mutableSharedFlowDescriptor = MutableSharedFlow<BluetoothGattDescriptor>(replay = 100)
    override val sharedFlowDescriptor: SharedFlow<BluetoothGattDescriptor>
        get() = mutableSharedFlowDescriptor.asSharedFlow()

    private val mutableStateFlowBond = MutableStateFlow<BleBondState?>(null)
    override val stateFlowBondState: StateFlow<BleBondState?>
        get() = mutableStateFlowBond.asStateFlow()
    override val bondState: BleBondState?
        get() = mutableStateFlowBond.value

    private val mutableSharedFlowNotifyCharacteristic = MutableSharedFlow<BleCharacteristicNotify>(replay = 100)
    override val notifiedCharacteristic: List<BluetoothGattCharacteristic>
        get() = TODO("Not yet implemented")
    override val sharedFlowCharacteristicNotify: SharedFlow<BleCharacteristicNotify>
        get() = mutableSharedFlowNotifyCharacteristic.asSharedFlow()

    override fun bondRequest(address: String): Boolean {
        TODO("Not yet implemented")
    }

    val scanIdling: ScanIdling = ScanIdling.getInstance(this)
    val connectingIdling: ConnectingIdling = ConnectingIdling.getInstance(this)
    val disconnectingIdling: DisconnectingIdling = DisconnectingIdling.getInstance(this)

    override fun startScan(
        addresses: List<String>,
        names: List<String>,
        services: List<String>,
        stopOnFind: Boolean,
        filterRepeatable: Boolean,
        stopTimeout: Long
    ): Boolean {
        Log.d(logTag, "startScan($scanState)")
        if (scanState != BleScanManager.State.Scanning) {
            mutableStateFlowScanState.tryEmit(BleScanManager.State.Scanning)
            scope.launch {
                (1..10).forEach { i ->
                    delay(Random.nextLong(200, 1000))
                    mutableSharedFlowScanResult.tryEmit(
                        BleScanResult(
                            BleDevice(Random.nextBytes(6)
                                .joinToString (":") { String.format("%02X", it) },
                                String.format("BLE_%02d", i),
                                if (Random.nextBoolean()) BluetoothDevice.BOND_BONDED else BluetoothDevice.BOND_NONE),
                            Random.nextBoolean(),
                            Random.nextInt(-100, 0)))
                    if (scanState != BleScanManager.State.Scanning) return@forEach
                }
                stopScan()
            }
        }
        return true
    }

    init {
        scope.launch {
            sharedFlowBleScanResult.collect {
                mutableScanResults.add(it)
            }
        }
    }


    override fun stopScan() {
        Log.d(logTag, "stopScan()")
        mutableStateFlowScanState.tryEmit(BleScanManager.State.Stopped)
    }

    private fun generateRandomBle(bleDevice: BleDevice): BleGatt {
        val services = mutableListOf<BluetoothGattService>()
        (0..1).forEach { _ ->
            val service = BluetoothGattService(UUID.randomUUID(), 0)
            (0..Random.nextInt(1,5)).forEach { _ ->
                val characteristic = BluetoothGattCharacteristic(UUID.randomUUID(), 0, 0)
                characteristic.value = Random.nextBytes(Random.nextInt(1,10))
                (0..1).forEach { _ ->
                    val descriptor = BluetoothGattDescriptor(UUID.randomUUID(), 0)
                    descriptor.value = ByteArray(1) {
                        Random.nextInt(0, 2).toByte() }
                    characteristic.addDescriptor(descriptor)
                }
                service.addCharacteristic(characteristic)
            }
            services.add(service)
        }
        return BleGatt(bleDevice, services)
    }

    override fun connect(address: String): BleGatt? {
        Log.d(logTag, "connect($address)")
        scanResults.find { it.device.address == address }?.let { scanResult ->
            val bleGatt = generateRandomBle(scanResult.device)
            mutableStateFlowConnectState.tryEmit(BleGattManager.State.Connected)
            // mutableStateFlowBluetoothGatt.tryEmit(bleGatt)
            scope.launch {
                mutableStateFlowConnectState.tryEmit(BleGattManager.State.Connecting)
                delay(Random.nextLong(300, 1500))
                mutableStateFlowConnectState.tryEmit(BleGattManager.State.Connected)
            }

            return bleGatt
        }

        return null
    }

    override fun disconnect() {
        Log.d(logTag, "disconnect()")
        scope.launch {
            mutableStateFlowConnectState.tryEmit(BleGattManager.State.Disconnecting)
            delay(Random.nextLong(100, 500))
            mutableStateFlowConnectState.tryEmit(BleGattManager.State.Disconnected)
            // mutableStateFlowBleGatt.tryEmit(null)
        }
    }

    override fun writeGattData(bleGattData: BleGattItem) {

    }

    override fun readCharacteristic(bluetoothGattCharacteristic: BluetoothGattCharacteristic) : Boolean {
        return  false

    }

    override fun readGattData(bleGattData: BleGattItem): Boolean {
        return false
    }

    override fun notifyCharacteristic(bleGattData: BleGattItem) { }

    override fun readDescriptor(bluetoothGattDescriptor: BluetoothGattDescriptor) : Boolean {
        return false
    }

    override fun notifyCharacteristic(
        bluetoothGattCharacteristic: BluetoothGattCharacteristic) {
    }

    override fun isCharacteristicNotified(bluetoothGattCharacteristic: BluetoothGattCharacteristic): Boolean {
        return true
    }

    override fun onDestroy() {
        TODO("Not yet implemented")
    }
}