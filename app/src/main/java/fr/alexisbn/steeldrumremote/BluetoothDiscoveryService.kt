package fr.alexisbn.steeldrumremote

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import java.util.UUID

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

    fun fetchDeviceUuids(
        context: Context,
        device: BluetoothDevice,
        onUuidsFetched: (List<UUID>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val filter = IntentFilter(BluetoothDevice.ACTION_UUID)

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                context.unregisterReceiver(this)

                val action = intent?.action
                if (action == BluetoothDevice.ACTION_UUID) {
                    val uuidArray = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID, ParcelUuid::class.java)
                    } else {
                        intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID)
                    }

                    if (!uuidArray.isNullOrEmpty()) {
                        val uuids = uuidArray.mapNotNull { (it as? ParcelUuid)?.uuid }
                        onUuidsFetched(uuids)
                    } else {
                        onError(Exception("No UUID found for device"))
                    }
                }
            }
        }

        try {
            context.registerReceiver(receiver, filter)
            val success = device.fetchUuidsWithSdp()
            if (!success) {
                context.unregisterReceiver(receiver)
                onError(Exception("fetchUuidsWithSdp failed"))
            }
        } catch (e: Exception) {
            context.unregisterReceiver(receiver)
            onError(e)
        }
    }
}