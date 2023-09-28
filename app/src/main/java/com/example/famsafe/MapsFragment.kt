package com.example.famsafe

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MapsFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var googleMap: GoogleMap

    private val callback = OnMapReadyCallback { map ->
        googleMap = map

        val bundle = arguments
        if (bundle != null && bundle.containsKey("otherLat") && bundle.containsKey("otherLong")) {
            // Latitude and longitude values are passed from HomeFragment
            val otherLat = bundle.getDouble("otherLat", 0.0)
            val otherLong = bundle.getDouble("otherLong", 0.0)
            val name = bundle.getString("otherName")

            if (otherLat != 0.0 && otherLong != 0.0) {
                val location = LatLng(otherLat, otherLong)
                googleMap.addMarker(MarkerOptions().position(location).title(name))
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(location))
            } else {
                Log.d("MapsFragment", "Invalid latitude or longitude values")
            }
        } else {
            // Bundle is empty, show current user's location from Firestore
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val userEmail = currentUser.email
                // Initialize Firestore
                val db = FirebaseFirestore.getInstance()

                // Reference to the document containing latitude and longitude
                val docRef = db.collection("users").document(userEmail!!)

                // Retrieve data from Firestore
                docRef.get()
                    .addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.exists()) {
                            val latitude = (documentSnapshot.getString("lat"))?.toDouble()
                            val longitude = (documentSnapshot.getString("long"))?.toDouble()
                            val name = (documentSnapshot.getString("name"))
                            if (latitude != null && longitude != null) {
                                // Print latitude and longitude
                                Log.d("lat", latitude.toString())
                                Log.d("long", longitude.toString())

                                // Add a marker for the user's location
                                val location = LatLng(latitude, longitude)
                                googleMap.addMarker(MarkerOptions().position(location).title(name))
                                googleMap.moveCamera(CameraUpdateFactory.newLatLng(location))
                            } else {
                                Log.d("MapsFragment", "Invalid latitude or longitude")
                            }
                        } else {
                            // Handle the case where the document does not exist
                            Log.d("MapsFragment", "Document does not exist")
                        }
                    }
                    .addOnFailureListener { e ->
                        // Handle errors while fetching data from Firestore
                        Log.e("MapsFragment", "Error fetching data from Firestore", e)
                    }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }
}
