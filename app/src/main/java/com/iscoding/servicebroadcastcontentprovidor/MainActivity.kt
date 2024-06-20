package com.iscoding.servicebroadcastcontentprovidor

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.iscoding.servicebroadcastcontentprovidor.ui.theme.ServiceBroadCastContentProvidorTheme
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        for the foreground  + should check permissions
//        val serviceIntent = Intent(this, HelloService::class.java)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            ContextCompat.startForegroundService(this, serviceIntent)
//        } else {
//            startService(serviceIntent)
//        }
        val serviceIntent = Intent(this, HelloService::class.java).apply {
            putExtra(HelloService.EXTRA_NUMBER1, 4)
            putExtra(HelloService.EXTRA_NUMBER2, 4)
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        setContent {

            ServiceBroadCastContentProvidorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->


                }
            }
        }
    }
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onResume() {
        super.onResume()
        // Register the sum result receiver for ACTION_SUM_RESULT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            registerReceiver(
                sumResultReceiver,
                IntentFilter(HelloService.ACTION_SUM_RESULT), RECEIVER_NOT_EXPORTED
            )
        }else{
            registerReceiver(
                sumResultReceiver,
                IntentFilter(HelloService.ACTION_SUM_RESULT)
            )
        }

    }

    override fun onPause() {
        super.onPause()
        // Unregister the sum result receiver
        unregisterReceiver(sumResultReceiver)
    }
}
private val sumResultReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            if (it.action == HelloService.ACTION_SUM_RESULT) {
                val sumResult = it.getIntExtra(HelloService.EXTRA_SUM_RESULT, 0)
                Log.d("ISLAM","Sum Result: $sumResult" )
            }
        }
    }
}