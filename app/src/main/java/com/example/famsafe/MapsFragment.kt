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

    private val callback = OnMapReadyCallback { googleMap ->
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userEmail = currentUser.email
            if (context != null) {
                Toast.makeText(context, userEmail, Toast.LENGTH_SHORT).show()
            }
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
                        val name=(documentSnapshot.getString("name"))
                        if (latitude != null && longitude != null) {
                            // Print latitude and longitude

                            Log.d("lat",latitude.toString())
//                            Log.d("name",documentSnapshot.getDouble("name").toString())

                            Log.d("long",longitude.toString())

//                            // TODO: Use latitude and longitude as needed in your fragment
                        } else {
                            print("lol")

                        }
                        if (context != null) {
                            Toast.makeText(context, latitude.toString(), Toast.LENGTH_SHORT).show()
                            Toast.makeText(context, longitude.toString(), Toast.LENGTH_SHORT).show()
                        }
                        if (latitude != null && longitude != null) {
                            val location = LatLng(latitude, longitude)
                            googleMap.addMarker(MarkerOptions().position(location).title(name))
                            googleMap.moveCamera(CameraUpdateFactory.newLatLng(location))
                        }

                    } else {
                        // Handle the case where the document does not exist
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
