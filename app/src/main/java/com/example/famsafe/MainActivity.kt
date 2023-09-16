package com.example.famsafe


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.famsafe.databinding.ActivityMainBinding

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
                    inflateFragment(ProfileFragment())

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