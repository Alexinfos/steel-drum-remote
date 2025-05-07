package fr.alexisbn.steeldrumremote

import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.alexisbn.steeldrumremote.databinding.ActivityDeviceSelectBinding

class DeviceSelectActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDeviceSelectBinding

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: DeviceListAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var viewAdapterListener: DeviceListAdapter.Callback

    private var results = arrayListOf<BluetoothDevice>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeviceSelectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewManager = LinearLayoutManager(this)

        viewAdapterListener = object : DeviceListAdapter.Callback {
            override fun startRemoteActivity(device: BluetoothDevice) {
                Log.d("START ACTIVITY", "Clicked : ${device.name} - ${device.address}")
                val intent = Intent(this@DeviceSelectActivity, RemoteActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra("device", device)

                startActivity(intent)
            }
        }

        viewAdapter = DeviceListAdapter(results, viewAdapterListener)

        recyclerView = (binding.devicesRecyclerView).apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

        BluetoothDiscoveryService.initialize(this)
    }

    private fun onDeviceDiscovered(device: BluetoothDevice) {
        Log.d("DISCOVERY", "Found : ${device.name} - ${device.address}")
        if (results.none { it.address == device.address } && device.name != null) {
            results.add(device)
            viewAdapter.notifyItemInserted(results.size - 1)
            Log.d("DISCOVERY", "Adding device ${device.address}")
        }
    }

    override fun onPause() {
        super.onPause()
        BluetoothDiscoveryService.stopDiscovery(this)
    }

    override fun onResume() {
        super.onResume()
        BluetoothDiscoveryService.startDiscovery(this) { device -> onDeviceDiscovered(device) }
    }

    class DeviceListAdapter(
        private val devices: ArrayList<BluetoothDevice>,
        private var callback: Callback?
    ) : RecyclerView.Adapter<DeviceListAdapter.ViewHolder>() {
        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val deviceNameTextView: TextView = v.findViewById(R.id.deviceNameTextView)
            val macTextView: TextView = v.findViewById(R.id.macTextView)
        }

        interface Callback {
            fun startRemoteActivity(device: BluetoothDevice)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_device, parent, false)

            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.deviceNameTextView.text = devices[position].name
            holder.macTextView.text = devices[position].address

            holder.itemView.setOnClickListener {
                callback?.startRemoteActivity(devices[position])
            }
        }

        override fun getItemCount() = devices.size
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }
}