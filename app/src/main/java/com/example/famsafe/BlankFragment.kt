package com.example.famsafe

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [BlankFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BlankFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_blank, container, false)

        // Authenticate the user
        val auth = Firebase.auth
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // User is logged in
            val userEmail = currentUser.email

            // Initialize Firestore
            val db = FirebaseFirestore.getInstance()

            // Reference to the document containing latitude and longitude
            val docRef = db.collection("users").document(userEmail!!)

            // Retrieve data from Firestore
            docRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val latitude = documentSnapshot.getDouble("lat")?.toDouble()
                        val longitude = documentSnapshot.getDouble("long")?.toDouble()

                        if (latitude != null && longitude != null) {
                            // Print latitude and longitude

                            Log.d("lat",latitude.toString())
//                            Log.d("name",documentSnapshot.getDouble("name").toString())

                            Log.d("long",longitude.toString())


                            // TODO: Use latitude and longitude as needed in your fragment
                        } else {
                            // Handle the case where latitude or longitude is missing
                        }
                    } else {
                        // Handle the case where the document does not exist
                    }
                }
                .addOnFailureListener { e ->
                    // Handle any errors that occurred during the retrieval
                    println("Error retrieving data: $e")
                }
        } else {
            // User is not logged in
            // Handle the case where the user is not authenticated
        }

        return view
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment BlankFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BlankFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}