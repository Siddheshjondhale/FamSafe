package com.example.famsafe
import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat.isLocationEnabled
import androidx.fragment.app.Fragment
import com.example.famsafe.databinding.ActivityMainBinding
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
    )
    val permissionCode = 78

    private lateinit var batteryPercentageListener: BatteryPercentageListener
    lateinit var binding: ActivityMainBinding

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                val batteryPercentage = getBatteryPercentage()
                updateBatteryPercentageInDatabase(batteryPercentage)
            }
        }
    }

    private val locationUpdateHandler = Handler(Looper.getMainLooper())
    private val locationUpdateRunnable = object : Runnable {
        override fun run() {
            updateLocationToFirestore()
            locationUpdateHandler.postDelayed(this, 5 * 60 * 100) // 2 minutes interval
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        var batteryPercentage = getBatteryPercentage()
        updateBatteryPercentageInDatabase(batteryPercentage)

        setUpLocationListener()

        if (isAllPermissionsGranted()) {
            if (isLocationEnabled(this)) {
                // Location permissions are granted and GPS is enabled, continue
            } else {
                showGPSNotEnabledDialog(this)
            }
        } else {
            askForPermission()
        }

        binding.bottomBar.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    inflateFragment(HomeFragment())
                }
                R.id.nav_dashboard -> {
                    inflateFragment(MapsFragment())
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
        val name = currentUser?.displayName.toString()
        val mail = currentUser?.email.toString()
        val phoneNumber = currentUser?.phoneNumber
        val imageUrl = currentUser?.photoUrl.toString()

        if (phoneNumber != null) {
            // Handle the case where the phone number is available
            val phoneNumberString = phoneNumber.toString()
            // Do something with phoneNumberString
        } else {
            // Handle the case where the phone number is not available
            // You can provide a default value or display a message
        }

        val db = Firebase.firestore

        val user = hashMapOf(
            "name" to name,
            "mail" to mail,
            "phoneNumber" to phoneNumber,
            "imageUrl" to imageUrl
        )

        db.collection("users").document(mail).set(user).addOnSuccessListener {
        }.addOnFailureListener {
            Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
            Log.d("testhaibhai", it.toString())
        }

        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        batteryPercentage = getBatteryPercentage()
        updateBatteryPercentageInDatabase(batteryPercentage)

        // Start location updates when MainActivity is created
        startLocationUpdates()
    }

    private fun isAllPermissionsGranted(): Boolean {
        for (item in permissions) {
            if (ContextCompat
                    .checkSelfPermission(
                        this,
                        item
                    ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    private fun askForPermission() {
        ActivityCompat.requestPermissions(this, permissions, permissionCode)
    }

    private fun setUpLocationListener() {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val locationRequest = LocationRequest().setInterval(2000).setFastestInterval(2000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    for (location in locationResult.locations) {
                        Log.d("Location89", "onLocationResult: latitude ${location.latitude}")
                        Log.d("Location89", "onLocationResult: longitude ${location.longitude}")

                        val currentUser = FirebaseAuth.getInstance().currentUser
                        val mail = currentUser?.email.toString()

                        val db = Firebase.firestore

                        val locationData = mutableMapOf<String, Any>(
                            "lat" to location.latitude.toString(),
                            "long" to location.longitude.toString(),
                        )

                        db.collection("users").document(mail).update(locationData)
                            .addOnSuccessListener {

                            }.addOnFailureListener {

                            }
                    }
                }
            },
            Looper.myLooper()
        )
    }

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager: LocationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun showGPSNotEnabledDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Enable GPS")
            .setMessage("required_for_this_app")
            .setCancelable(false)
            .setPositiveButton("enable_now") { _, _ ->
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(batteryReceiver)
        stopLocationUpdates()
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

        val userMap: Map<String, Any> = user

        db.collection("users").document(mail).update(userMap)
            .addOnSuccessListener {
            }
            .addOnFailureListener { e ->
                Log.d("testhaibhai", e.toString())
            }
    }

    private fun startLocationUpdates() {
        locationUpdateHandler.postDelayed(locationUpdateRunnable, 0) // Start immediately
    }

    private fun stopLocationUpdates() {
        locationUpdateHandler.removeCallbacks(locationUpdateRunnable)
    }

    private fun updateLocationToFirestore() {
        Toast.makeText(this, "updated location", Toast.LENGTH_SHORT).show()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val mail = currentUser?.email.toString()

        val db = Firebase.firestore

        // Get the location and update it to Firestore
        // This code will be called every 2 minutes as scheduled
        // Replace this with your location retrieval and Firestore update logic
        val locationData = mutableMapOf<String, Any>(
            // Your location data here
        )

        db.collection("users").document(mail).update(locationData)
            .addOnSuccessListener {

            }.addOnFailureListener {

            }
    }
}

