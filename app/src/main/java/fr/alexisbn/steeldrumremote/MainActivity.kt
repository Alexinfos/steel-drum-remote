package fr.alexisbn.steeldrumremote

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import fr.alexisbn.steeldrumremote.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val PERMISSIONS_BT = mutableListOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        binding.connectButton.setOnClickListener {
            if (hasAllBluetoothPermissions(this@MainActivity)) {
                val intent = Intent(this@MainActivity, DeviceSelectActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                startActivity(intent)
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    getRequiredPermissions(),
                    1038
                )
            }
        }

        binding.settingsButton.setOnClickListener {
            val intent = Intent(this@MainActivity, SettingsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            startActivity(intent)
        }
    }

    private fun getRequiredPermissions(): Array<String> {
        val perms = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+
            perms.add(Manifest.permission.BLUETOOTH_SCAN)
            perms.add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            // Android < 12
            perms.addAll(PERMISSIONS_BT)
            perms.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        return perms.toTypedArray()
    }

    private fun hasAllBluetoothPermissions(context: Context): Boolean {
        val required = getRequiredPermissions()
        return required.all {
            ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}