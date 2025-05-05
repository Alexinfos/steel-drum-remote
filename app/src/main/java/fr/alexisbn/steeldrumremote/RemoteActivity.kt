package fr.alexisbn.steeldrumremote

import android.bluetooth.BluetoothDevice
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import fr.alexisbn.steeldrumremote.databinding.ActivityRemoteBinding
import kotlinx.coroutines.launch
import java.util.UUID

class RemoteActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "RemoteActivity"
    }
    private lateinit var binding: ActivityRemoteBinding

    private lateinit var device: BluetoothDevice
    private lateinit var bluetoothService: BluetoothService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRemoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (!intent.hasExtra("device")) {
            Log.e("RemoteActivity", "No device provided!")
            this.finish()
        }

        device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("device", BluetoothDevice::class.java)!!
        } else {
            intent.getParcelableExtra("device")!!
        }

        bluetoothService = BluetoothService(this)

        for (i in 0..<binding.keyboardLinearLayout.childCount) {
            Log.d(TAG, "i = $i")
            val v: View = binding.keyboardLinearLayout.getChildAt(i)
            v.setOnClickListener {
                Log.d(TAG, "Button $i clicked!")
                if (bluetoothService.isConnected()) {
                    bluetoothService.sendCommand("play $i\n")
                }
            }
        }
    }

    override fun onPause() {
        lifecycleScope.launch {
            bluetoothService.disconnect()
        }
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            bluetoothService.connect(device, ::onBluetoothConnectionEstablished, ::onBluetoothConnectionFailed)
        }
    }

    private fun onBluetoothConnectionEstablished() {
        Log.d(TAG, "Bluetooth connection established!")
        bluetoothService.sendCommand("TEST1\n")
        bluetoothService.sendCommand("TEST2\n")
        bluetoothService.sendCommand("TEST3\n")
    }

    private fun onBluetoothConnectionFailed() {
        Log.e(TAG, "Failed to establish bluetooth connection")
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }
}