package com.example.famsafe

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.famsafe.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var batteryPercentageListener: BatteryPercentageListener
    lateinit var binding: ActivityMainBinding

    // Create a BroadcastReceiver to listen for battery changes
    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                val batteryPercentage = getBatteryPercentage()
                updateBatteryPercentageInDatabase(batteryPercentage)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomBar.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    inflateFragment(HomeFragment())
                }
                R.id.nav_dashboard -> {
                    inflateFragment(GuardFragment())
                }
                R.id.nav_guard -> {
                    inflateFragment(GuardFragment())
                }
                R.id.nav_profile -> {
                    inflateFragment(ProfileFragment())
                }
            }
            true
        }

        binding.bottomBar.selectedItemId = R.id.nav_home

        val currentUser = FirebaseAuth.getInstance().currentUser
        val mail = currentUser?.email.toString()

        // Register the BroadcastReceiver to listen for battery changes
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        // Initialize the initial battery percentage in the database
        val batteryPercentage = getBatteryPercentage()
        updateBatteryPercentageInDatabase(batteryPercentage)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the BroadcastReceiver when the activity is destroyed
        unregisterReceiver(batteryReceiver)
    }

    private fun inflateFragment(newInstance: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, newInstance)
        transaction.commit()
    }

    private fun getBatteryPercentage(): Int {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            applicationContext.registerReceiver(null, ifilter)
        }

        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1

        return if (level != -1 && scale != -1) {
            (level * 100 / scale)
        } else {
            -1
        }
    }

    private fun updateBatteryPercentageInDatabase(batteryPercentage: Int) {
        val db = Firebase.firestore
        val mail = FirebaseAuth.getInstance().currentUser?.email.toString()

        val user = hashMapOf("battery" to batteryPercentage.toString())

        // Explicitly cast the user HashMap to the required Map type
        val userMap: Map<String, Any> = user

        db.collection("users").document(mail).update(userMap)
            .addOnSuccessListener {
                // Successfully updated the battery percentage in the database
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
                Log.d("testhaibhai", e.toString())
            }
    }

}
