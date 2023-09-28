package com.example.famsafe

import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.famsafe.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var batteryPercentageListener: BatteryPercentageListener
    private var listMembers = mutableListOf<MemberModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var batteryinfo: String? = null
        val mContext = requireContext()

        val adapter = MemberAdapter(listMembers) { position ->
            // Handle the item click event here
            val clickedMember = listMembers[position]
            val otherUserEmail =clickedMember.emailval
//            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            val firestore = FirebaseFirestore.getInstance()
            val usersCollection = firestore.collection("users")
            val userDocument = usersCollection.document(otherUserEmail)

    userDocument.get().addOnSuccessListener { documentSnapshort->

        if (documentSnapshort.exists()){
            var latitude = ((documentSnapshort.getString("lat"))?.toDouble()) ?: 0.0
            var longitude = ((documentSnapshort.getString("long"))?.toDouble()) ?: 0.0
            var name=(documentSnapshort.getString("name"))
//code to create a bundle and pass to the mapfragment
            val bundle = Bundle()
            bundle.putDouble("otherLat", latitude)
            bundle.putDouble("otherLong", longitude)
            bundle.putString("otherName",name)
            val mapsFragment = MapsFragment()
            mapsFragment.arguments = bundle
             val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.container, mapsFragment)
            transaction.addToBackStack(null) // Optional: Add to back stack if needed
            transaction.commit()

        }
    }

        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        val userEmail = currentUser?.email
        if (userEmail != null) {
            val firestore = FirebaseFirestore.getInstance()
            val usersCollection = firestore.collection("users")
            val userDocument = usersCollection.document(userEmail)
            userDocument.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        var latitude = ((documentSnapshot.getString("lat"))?.toDouble()) ?: 0.0
                        var longitude = ((documentSnapshot.getString("long"))?.toDouble()) ?: 0.0
                        batteryinfo = documentSnapshot.getString("battery")
                        listMembers.clear()
                        listMembers.add(
                            MemberModel(
                                currentUser.displayName.toString(),
                                convertCoordinatesToAddress(latitude, longitude),
                                batteryinfo.toString(),
                                "220",
                                userEmail
                            )
                        )

                        val invitesCollection = userDocument.collection("invites")
                        invitesCollection.whereEqualTo("invite_status", 1)
                            .get()
                            .addOnSuccessListener { invitesQuerySnapshot ->
                                for (inviteDocument in invitesQuerySnapshot.documents) {
                                    val otherUserEmail = inviteDocument.id
                                    val otherUserDoc = firestore.collection("users")
                                        .document(otherUserEmail)

                                    otherUserDoc.get()
                                        .addOnSuccessListener { otherUserSnapshot ->
                                            if (otherUserSnapshot.exists()) {
                                                val otherUserName =otherUserSnapshot.getString("name")?: "Unknown"

                                                var otherUserLatitude =
                                                    otherUserSnapshot.getString("lat")?.toDouble() ?: 0.0
                                                var otherUserLongitude =
                                                    otherUserSnapshot.getString("long")?.toDouble() ?: 0.0

                                                var otheruserbattery =
                                                    otherUserSnapshot.getString("battery")
                                                listMembers.add(
                                                    MemberModel(
                                                        otherUserName,
                                                        convertCoordinatesToAddress(
                                                            otherUserLatitude,
                                                            otherUserLongitude
                                                        ),
                                                        otheruserbattery.toString(),
                                                        "220",
                                                        otherUserEmail,

                                                    )
                                                )

                                                adapter.notifyDataSetChanged()
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            // Handle errors while fetching other user data
                                        }
                                }
                            }
                            .addOnFailureListener { e ->
                                // Handle errors while fetching invites
                            }
                    } else {
                        // Handle the case where the user's document doesn't exist
                    }
                }
                .addOnFailureListener { e ->
                    // Handle any errors that occur while fetching data from Firestore
                }
        }

        binding.recyclerMember.layoutManager = LinearLayoutManager(mContext)
        binding.recyclerMember.adapter = adapter

        batteryPercentageListener = BatteryPercentageListener(mContext)

        batteryPercentageListener.observe(viewLifecycleOwner, Observer { batteryPercentage ->
            val updatedListMembers = listMembers.mapIndexed { index, member ->
                if (index == 0) {
                    MemberModel(
                        member.name,
                        member.address,
                        "$batteryPercentage%",
                        member.distance,
                        member.emailval,
                    )
                } else {
                    member // Keep the other members unchanged
                }
            }
            listMembers.clear()
            listMembers.addAll(updatedListMembers)
            adapter.notifyDataSetChanged()
        })
    }

    //convert the coordinate into  address
    private fun convertCoordinatesToAddress(latitude: Double, longitude: Double): String {
        val context = context
        if (context != null) {
            val geocoder = Geocoder(context)
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)

            if (addresses?.isNotEmpty() == true) {
                val address = addresses[0]
                val fullAddress = address.getAddressLine(0)

                // Use fullAddress as the address in your application
                return fullAddress ?: "Address not found" // Return fullAddress or a default message
            } else {
                // Handle the case where no address was found for the given coordinates
                return "Address not found" // Return a default message
            }
        } else {
            // Handle the case where the fragment is not attached to a context
            return "Context not available"
        }
    }
}
