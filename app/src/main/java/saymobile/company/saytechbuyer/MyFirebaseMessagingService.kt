package saymobile.company.saytechbuyer

import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import saymobile.company.saytechbuyer.view.DashboardActivity

private const val CHANNEL_ID = "ORDER_CHANNEL_ID"


class MyFirebaseMessagingService : FirebaseMessagingService(){

    override fun onNewToken(token: String) {
        Log.d("Token", "Token refreshed: $token")
        sendRegistrationTokenToServer(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val notificationId = 1
        val pendingIntent = NavDeepLinkBuilder(this)
            .setComponentName(DashboardActivity::class.java)
            .setGraph(R.navigation.say_buyer_navigation)
            .setDestination(R.id.transactionsFragment)
            .createPendingIntent()

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_saytech_logo)
            .setContentTitle(remoteMessage.notification?.title)
            .setContentText(remoteMessage.notification?.body)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)){
            notify(notificationId, builder.build())
        }
    }

    private fun sendRegistrationTokenToServer(token: String){
        val mFirebaseDatabase = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val tokenUpdate = hashMapOf<String, Any>("pushToken" to token)
        val userId = currentUser!!.uid
        mFirebaseDatabase.collection("buyers")
            .document(userId).update(tokenUpdate).addOnSuccessListener {
                Log.d("pushTokenUpdate", "Successful")
            }.addOnFailureListener{
                Log.d("pushTokenUpdate", "Failed")
            }.addOnCompleteListener {
                Log.d("pushTokenUpdate", "Completed")
            }
    }
}