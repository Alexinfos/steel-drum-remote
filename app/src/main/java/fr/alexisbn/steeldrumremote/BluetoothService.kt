package fr.alexisbn.steeldrumremote

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothSocket
import android.bluetooth.BluetoothStatusCodes
import android.content.Context
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.util.UUID

class BluetoothService(private val context: Context) {

    companion object {
        private const val TAG = "BluetoothService"
        private val SERVICE_UUID: UUID = UUID.fromString("0000abf0-0000-1000-8000-00805f9b34fb")
        private val WRITE_CHAR_UUID: UUID = UUID.fromString("0000abf1-0000-1000-8000-00805f9b34fb")
        private val READ_CHAR_UUID: UUID = UUID.fromString("0000abf2-0000-1000-8000-00805f9b34fb")

    }

    private var bluetoothGatt: BluetoothGatt? = null
    private var writeCharacteristic: BluetoothGattCharacteristic? = null
    private var isConnected = false
    private var onConnected: (() -> Unit)? = null
    private var onConnectionFailed: (() -> Unit)? = null

    fun connect(device: BluetoothDevice, onSuccess: () -> Unit, onFailure: () -> Unit) {
        onConnected = onSuccess
        onConnectionFailed = onFailure

        bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }

    fun sendCommand(command: String): Boolean {
        val char = writeCharacteristic ?: return false
        val bytes = command.toByteArray(Charsets.UTF_8)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val status = bluetoothGatt?.writeCharacteristic(char, bytes, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
            status == BluetoothStatusCodes.SUCCESS
        } else {
            char.value = bytes
            bluetoothGatt?.writeCharacteristic(char) == true
        }
    }

    fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        writeCharacteristic = null
        isConnected = false
        Log.d(TAG, "Disconnected from GATT")
    }

    fun isConnected(): Boolean = isConnected

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Connected to GATT, discovering services")
                gatt?.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Disconnected from GATT")
                isConnected = false
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "Service discovery failed")
                onConnectionFailed?.invoke()
                return
            }

            val service = gatt?.getService(SERVICE_UUID)
            val characteristic = service?.getCharacteristic(WRITE_CHAR_UUID)

            if (characteristic != null) {
                writeCharacteristic = characteristic
                isConnected = true
                Log.d(TAG, "Service and characteristic found, ready to send")
                onConnected?.invoke()
            } else {
                Log.e(TAG, "Write characteristic not found")
                onConnectionFailed?.invoke()
            }
        }
    }
}