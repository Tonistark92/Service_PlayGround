package com.iscoding.servicebroadcastcontentprovidor

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.iscoding.servicebroadcastcontentprovidor.ui.theme.ServiceBroadCastContentProvidorTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
class MainActivity : ComponentActivity() {
    private lateinit var mService: HelloService
    //    private val viewModel: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         var myMessenger: Messenger? = null
//        for the foreground  + should check permissions
//        val serviceIntent = Intent(this, HelloService::class.java)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            ContextCompat.startForegroundService(this, serviceIntent)
//        } else {
//            startService(serviceIntent)
//        }
        // that connection for th ibinder
//        val serviceConnection = object : ServiceConnection {
//            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
//                Log.d("ISLAM", "Connected to Activity ")
//
//                val binder = service as HelloService.HelloBinder
//                lifecycleScope.launch {
//                    mService=   binder.getService()
//                    mService.sumResultFlow.collect {
//                        Toast.makeText(
//                            this@MainActivity,
//                            it.toString() + "from Activity",
//                            Toast.LENGTH_LONG
//                        ).show()
//                        Log.d("ISLAM", it.toString())
//                    }
//                }
//            }
//
//            override fun onServiceDisconnected(name: ComponentName?) {
//
//            }
//        }

         val myMessengerConnection = object : ServiceConnection {

            override fun onServiceConnected(className: ComponentName, service: IBinder) {
                // This is called when the connection with the service has been
                // established, giving us the object we can use to
                // interact with the service.  We are communicating with the
                // service using a Messenger, so here we get a client-side
                // representation of that from the raw IBinder object.
                myMessenger = Messenger(service)
                // creaate and sen message with messenger
                val msg: Message = Message.obtain(null, 9, 0, 0)
                try {
                    myMessenger?.send(msg)
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
            }

            override fun onServiceDisconnected(className: ComponentName) {
                // This is called when the connection with the service has been
                // unexpectedly disconnected&mdash;that is, its process crashed.
                myMessenger = null
            }
        }
        val serviceIntent = Intent(this, HelloService::class.java).apply {
            putExtra(HelloService.EXTRA_NUMBER1, 2)
            putExtra(HelloService.EXTRA_NUMBER2, 4)
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
//            bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
            bindService(serviceIntent, myMessengerConnection, Context.BIND_AUTO_CREATE)

        } else {
            startService(serviceIntent)
//            bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
            bindService(serviceIntent, myMessengerConnection, Context.BIND_AUTO_CREATE)
        }

//        lifecycleScope.launch {
//            delay(5000)
//            viewModel.sumResult.collect { sum ->
//                Toast.makeText(this@MainActivity, sum.toString(), Toast.LENGTH_LONG).show()
//            }
//        }
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                sumResultReceiver,
                IntentFilter(HelloService.ACTION_SUM_RESULT), RECEIVER_NOT_EXPORTED
            )
        } else {
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
                Log.d("ISLAM", "Sum Result: $sumResult")
            }
        }
    }
}

