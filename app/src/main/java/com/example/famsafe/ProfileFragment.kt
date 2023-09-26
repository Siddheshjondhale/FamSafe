package com.example.famsafe

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.famsafe.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class ProfileFragment : Fragment() {

    lateinit var binding: FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        // Initialize Firebase Authentication
        val auth = FirebaseAuth.getInstance()

        // Check if a user is currently signed in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User is signed in, get their email
            val userEmail = currentUser.email

            // Reference the "title" TextView from the fragment's layout by its ID
            val emailTextView = view.findViewById<TextView>(R.id.Email)

            // Append the user's email to the existing text
            emailTextView.text = "Welcome, $userEmail" // Change the text as needed
        }

        val logoutbtn = view.findViewById<Button>(R.id.logout)

        logoutbtn.setOnClickListener {
            // Log the user out
            FirebaseAuth.getInstance().signOut()

            // Show a toast message
            Toast.makeText(activity, "Logged out successfully", Toast.LENGTH_SHORT).show()

            // Navigate to LoginActivity
            val intent = Intent(activity, SplashScreen::class.java)
            intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            )
            startActivity(intent)
            activity?.finish() // Finish the current activity to prevent going back to the profile fragment

        }




        var Edittextphone=view.findViewById<EditText>(R.id.Edittextphone)
        var sendsmsboy=view.findViewById<Button>(R.id.sendsmsboy)

            sendsmsboy.setOnClickListener{
                var newPhoneNumber=Edittextphone.text.toString().trim()

                if (newPhoneNumber.isNotEmpty()) {
                    // Update the phone number in Firestore
                    updatePhoneNumberInFirestore(newPhoneNumber)
                } else {
                    Toast.makeText(activity, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
                }

            }


        return view
    }

    private fun updatePhoneNumberInFirestore(newPhoneNumber: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()

        currentUser?.let { user ->
            val userEmail = user.email

            if (userEmail != null) {
                // Assuming you have a "users" collection in Firestore
                val userRef = db.collection("users").document(userEmail)

                // Update the "phonenumber" field in the user's document
                userRef.update("phoneNumber", newPhoneNumber)
                    .addOnSuccessListener {
                        Toast.makeText(
                            activity,
                            "Phone number updated successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            activity,
                            "Failed to update phone number: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
    }







}
