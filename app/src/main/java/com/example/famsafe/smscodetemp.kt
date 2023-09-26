//import android.Manifest
//import android.app.PendingIntent
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.telephony.SmsManager
//import android.widget.Button
//import android.widget.Toast
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import com.example.famsafe.ProfileFragment
//import com.example.famsafe.R
//
//val sendSmsButton = view.findViewById<Button>(R.id.sendsmsboy)
//
//
//sendSmsButton.setOnClickListener {
//    if (ContextCompat.checkSelfPermission(
//            requireContext(),
//            Manifest.permission.SEND_SMS
//        ) == PackageManager.PERMISSION_GRANTED
//    ) {
//        // Permission is granted, send the SMS
//        sendSMS()
//    } else {
//        // Permission is not granted, request it
//        ActivityCompat.requestPermissions(
//            requireActivity(),
//            arrayOf(Manifest.permission.SEND_SMS),
//            ProfileFragment.SMS_PERMISSION_REQUEST_CODE
//        )
//    }
//}
//private val phoneNumber = "9022846168" // Replace with the recipient's phone number
//private val message = "Help Help Check my last location" // Replace with the SMS message
//



//
//
//private fun sendSMS() {
//    try {
//        val smsManager = SmsManager.getDefault()
//        val sentIntent = PendingIntent.getBroadcast(
//            requireContext(), 0, Intent("SMS_SENT"), 0
//        )
//
//        // Send the SMS
//        smsManager.sendTextMessage(phoneNumber, null, message, sentIntent, null)
//
//        // Show a toast message indicating that the SMS has been sent
//        Toast.makeText(requireContext(), "SMS sent successfully", Toast.LENGTH_SHORT).show()
//    } catch (e: Exception) {
//        // Handle exceptions here
//        e.printStackTrace()
//        // Show a toast message if there's an error
//        Toast.makeText(requireContext(), "Failed to send SMS", Toast.LENGTH_SHORT).show()
//    }
//}
//
//companion object {
//    private const val SMS_PERMISSION_REQUEST_CODE = 123
//}