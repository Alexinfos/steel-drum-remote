package fr.alexisbn.steeldrumremote

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log

object BluetoothDiscoveryService {

    private const val TAG = "BluetoothDiscoveryService"

    private var adapter: BluetoothAdapter? = null
    private var discoveryCallback: ((BluetoothDevice) -> Unit)? = null

    fun initialize(context: Context) {
        adapter = context.getSystemService(BluetoothManager::class.java)?.adapter
    }

    fun startDiscovery(context: Context, onDeviceFound: (BluetoothDevice) -> Unit) {
        if (adapter?.isDiscovering == true) adapter?.cancelDiscovery()

        discoveryCallback = onDeviceFound

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        context.registerReceiver(receiver, filter)

        adapter?.startDiscovery()
    }

    fun stopDiscovery(context: Context) {
        adapter?.cancelDiscovery()
        try {
            context.unregisterReceiver(receiver)
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            if (intent?.action == BluetoothDevice.ACTION_FOUND) {
                val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                } else {
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                }
                device?.let { discoveryCallback?.invoke(it) }
            }
        }
    }
}