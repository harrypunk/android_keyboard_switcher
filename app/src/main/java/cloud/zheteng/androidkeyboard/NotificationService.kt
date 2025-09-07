package cloud.zheteng.androidkeyboard

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat

/**
 * A foreground service that manages a persistent notification to provide a quick
 * shortcut to the Android keyboard picker dialog.
 */
class NotificationService : Service() {

    // A unique ID for the notification channel and the notification itself.
    companion object {
        const val CHANNEL_ID = "KEYBOARD_SWITCH"
        const val NOTIFICATION_ID = 1
    }

    override fun onBind(intent: Intent?): IBinder? {
        // This service is not designed to be bound, so we return null.
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Create the notification channel (required for Android 8.0 and higher)
        createNotificationChannel()

        // Create an Intent that opens the system keyboard picker dialog.
        val keyboardPickerIntent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
            // THIS IS THE CRUCIAL PART
            // Add the new task flag to allow launching from a service context
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        // Use a PendingIntent to allow the system to launch the dialog.
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            keyboardPickerIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification.
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Keyboard Switch")
            .setContentText("change keyboard")
            .setSmallIcon(android.R.drawable.ic_dialog_dialer)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true) // Makes the notification persistent.
            .build()

        Log.d("ks", "start foreground")
        // Start the service in the foreground, displaying the notification.
        val svcType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }
        try {
            ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, svcType)
        } catch (e: Exception) {
            Log.d("ks","failed to start foreground", e)
        }

        // Return START_NOT_STICKY so the service is not recreated if it is killed.
        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "Switch Keyboard",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }
}
