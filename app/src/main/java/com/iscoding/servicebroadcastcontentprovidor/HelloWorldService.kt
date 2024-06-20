package com.iscoding.servicebroadcastcontentprovidor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class HelloService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)

    // for foreground services
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "SumServiceChannel"

        // for retrun with pending intent
        const val ACTION_SUM_RESULT = "com.example.myapp.SUM_RESULT"
        const val EXTRA_NUMBER1 = "number1"
        const val EXTRA_NUMBER2 = "number2"
        const val EXTRA_SUM_RESULT = "sumResult"
    }

    override fun onCreate() {
        super.onCreate()
        // Initialize any resources your service needs
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show()
        // if it will be foreground service if we used  startForegroundService(intent) or start service(intent) after 5 sec should call
        //startForeground(NOTIFICATION_ID, notification)
        createNotificationChannel()

        val notification = buildNotification()

        // Start the service as foreground
        startForeground(NOTIFICATION_ID, notification)
        // Launch a coroutine to handle the background work
        serviceScope.launch {
            handleServiceWork(startId, intent)
        }

        // If we get killed, after returning from here, restart with null intent untill new intent with data got send
        return START_STICKY
        // system wont start it after it is killed
//        return START_NOT_STICKY
        // system will restart the intent with the last intent the service got called with
//        return START_REDELIVER_INTENT
    }

    override fun onBind(intent: Intent?): IBinder? {
        // We don't provide binding, so return null
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()  // Cancel all coroutines when the service is destroyed
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show()
    }

    private suspend fun handleServiceWork(startId: Int, intent: Intent?) {
        // Simulate some work
        delay(3000)
        val number1 = intent?.getIntExtra(EXTRA_NUMBER1, 0) ?: 0
        val number2 = intent?.getIntExtra(EXTRA_NUMBER2, 0) ?: 0
        val sum = number2 + number1

        val resultIntent = Intent(ACTION_SUM_RESULT)
        resultIntent.putExtra(EXTRA_SUM_RESULT,  sum)

        // Send the result back using a broadcast
        withContext(Dispatchers.Main) {
            sendBroadcast(resultIntent)
        }
        // Stop the service using the startId, so that we don't stop
        // the service in the middle of handling another job
        stopSelf(startId)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Sum Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Sum Service")
            .setContentText("Calculating sum...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()
    }
}