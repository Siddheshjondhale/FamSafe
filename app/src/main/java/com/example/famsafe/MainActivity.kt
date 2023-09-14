package com.example.famsafe


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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



    lateinit var binding: ActivityMainBinding

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
                    inflateFragment(GuardFragment())
                }
            }

            true
        }

        binding.bottomBar.selectedItemId = R.id.nav_home



    }

    private fun inflateFragment(newInstance: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, newInstance)
        transaction.commit()
    }
}