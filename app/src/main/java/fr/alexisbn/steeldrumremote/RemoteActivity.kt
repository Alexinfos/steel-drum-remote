package fr.alexisbn.steeldrumremote

import android.bluetooth.BluetoothDevice
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
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

    private var melodyPlayer: MelodyPlayer? = null

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

        binding.playButton.setOnClickListener {
            if (melodyPlayer == null || !melodyPlayer!!.isRunning()) {
                melodyPlayer = MelodyPlayer(bluetoothService)
                melodyPlayer!!.start()
                binding.playButton.text = getString(R.string.stop)
            } else {
                melodyPlayer!!.stopMelody()
                binding.playButton.text = getString(R.string.play)
            }
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab == null || !tab.isSelected) return
                when (tab.position) {
                    0 -> {
                        binding.melodyConstraintLayout.visibility = View.GONE
                        binding.keyboardLinearLayout.visibility = View.VISIBLE
                    }
                    1 -> {
                        binding.keyboardLinearLayout.visibility = View.GONE
                        binding.melodyConstraintLayout.visibility = View.VISIBLE
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
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
        bluetoothService.sendCommand("hello\n")
    }

    private fun onBluetoothConnectionFailed() {
        Log.e(TAG, "Failed to establish bluetooth connection")
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }
}