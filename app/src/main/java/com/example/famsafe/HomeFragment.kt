package com.example.famsafe

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.famsafe.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.sql.Types.NULL

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var batteryPercentageListener: BatteryPercentageListener
    private var listMembers = mutableListOf<MemberModel>()
    private val smsHandler = Handler(Looper.getMainLooper())
    private val smsIntervalMillis = 20000 // 20 seconds in milliseconds
    private val activeSmsTasks = MutableList<Boolean?>(0) { null }
    private val smsRunnableMap = mutableMapOf<Int, Runnable>()

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

        val adapter = MemberAdapter(listMembers,
            onDistanceClick = { position ->
                // Handle the item click event here
                val clickedMember = listMembers[position]
                val otherUserEmail = clickedMember.emailval
                val firestore = FirebaseFirestore.getInstance()
                val usersCollection = firestore.collection("users")
                val userDocument = usersCollection.document(otherUserEmail)

                userDocument.get().addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        var latitude =
                            (documentSnapshot.getString("lat"))?.toDouble() ?: 0.0
                        var longitude =
                            (documentSnapshot.getString("long"))?.toDouble() ?: 0.0
                        var name = (documentSnapshot.getString("name"))
                        val bundle = Bundle()
                        bundle.putDouble("otherLat", latitude)
                        bundle.putDouble("otherLong", longitude)
                        bundle.putString("otherName", name)
                        val mapsFragment = MapsFragment()
                        mapsFragment.arguments = bundle
                        val transaction = parentFragmentManager.beginTransaction()
                        transaction.replace(R.id.container, mapsFragment)
                        transaction.addToBackStack(null) // Optional: Add to back stack if needed
                        transaction.commit()
                    }
                }
            },
            onSmsClick = { position ->
                // Handle sms click here
                Toast.makeText(requireContext(), "lolsms", Toast.LENGTH_SHORT).show()
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.SEND_SMS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val clickedMember = listMembers[position]
                    val otherUserEmail = clickedMember.emailval
                    val firestore = FirebaseFirestore.getInstance()
                    val usersCollection = firestore.collection("users")
                    val userDocument = usersCollection.document(otherUserEmail)

                    userDocument.get().addOnSuccessListener { documentsnapshort ->
                        if (documentsnapshort.exists()) {
                            var phoneNumber = (documentsnapshort.getString("phoneNumber"))
                            sendSMS(phoneNumber)
                        }
                    }
                } else {
                    // Permission is not granted, request it
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.SEND_SMS),
                        SMS_PERMISSION_REQUEST_CODE
                    )
                }
            },
            onStopsosclick = { position ->
                if (position >= 0 && position < activeSmsTasks.size) {
                    // Handle stopping the SMS task for the user at the specified position
                    activeSmsTasks[position] = false
                    Toast.makeText(requireContext(), "SOS Stopped", Toast.LENGTH_SHORT).show()

                    // Remove the corresponding Runnable from the smsHandler
                    val runnable = smsRunnableMap[position]
                    if (runnable != null) {
                        smsHandler.removeCallbacks(runnable)
                        smsRunnableMap.remove(position)
                    }
                }
            }
        )

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
                                userEmail,
                                NULL.toString(),
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
                                                val otherUserName =
                                                    otherUserSnapshot.getString("name") ?: "Unknown"

                                                var otherUserLatitude =
                                                    otherUserSnapshot.getString("lat")?.toDouble()
                                                        ?: 0.0
                                                var otherUserLongitude =
                                                    otherUserSnapshot.getString("long")?.toDouble()
                                                        ?: 0.0

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
                                                        NULL.toString(),

                                                        )
                                                )

                                                // Add a task for this member and initialize it with true
                                                activeSmsTasks.add(true)
                                                scheduleSmsSendingTask(activeSmsTasks.size - 1) // Schedule the task
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
                        NULL.toString(),
                    )
                } else {
                    member // Keep the other members unchanged
                }
            }
            listMembers.clear()
            listMembers.addAll(updatedListMembers)
            adapter.notifyDataSetChanged()
        })

        activeSmsTasks.clear()
        activeSmsTasks.addAll(listOf(true)) // Initialize the first task (current user)

        // Schedule the SMS sending task for all members
        for (position in 1 until listMembers.size) {
            activeSmsTasks.add(true)
            scheduleSmsSendingTask(position)
        }
    }

    private fun sendSMS(phoneNumber: String?) {
        try {
            val smsManager = SmsManager.getDefault()
            val sentIntent = PendingIntent.getBroadcast(
                requireContext(), 0, Intent("SMS_SENT"), 0
            )

            // Send the SMS
            smsManager.sendTextMessage(
                phoneNumber, null, "Help Help Check My last location", sentIntent, null
            )
        } catch (e: Exception) {
            // Handle exceptions here
            e.printStackTrace()
        }
    }

    companion object {
        private const val SMS_PERMISSION_REQUEST_CODE = 123
    }

    // Convert the coordinate into an address
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

    private fun scheduleSmsSendingTask(position: Int) {
        if (position >= 0 && position < activeSmsTasks.size && activeSmsTasks[position] == true) {
            val runnable = object : Runnable {
                override fun run() {
                    val clickedMember = listMembers[position]
                    val otherUserEmail = clickedMember.emailval
                    val firestore = FirebaseFirestore.getInstance()
                    val usersCollection = firestore.collection("users")
                    val userDocument = usersCollection.document(otherUserEmail)
                    val context = context

                    userDocument.get().addOnSuccessListener { documentsnapshot ->
                        if (documentsnapshot.exists()) {
                            val phoneNumber = documentsnapshot.getString("phoneNumber")

                            // Check if the task is still active before sending the SMS
                            if (activeSmsTasks[position] == true) {
                                sendSMS(phoneNumber)
                                // Reschedule the task
                                scheduleSmsSendingTask(position)
                            }
                        }
                    }
                }
            }

            // Store the runnable in the map for later removal
            smsRunnableMap[position] = runnable

            smsHandler.postDelayed(runnable, smsIntervalMillis.toLong())
        }
    }
}
