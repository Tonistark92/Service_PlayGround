package com.iscoding.servicebroadcastcontentprovidor

import android.annotation.SuppressLint
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _sumResult = MutableStateFlow(0)
    val sumResult: StateFlow<Int> get() = _sumResult

    @SuppressLint("StaticFieldLeak")
    private var helloService: HelloService? = null
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as HelloService.HelloBinder
            helloService = binder.getService()
            viewModelScope.launch {
                helloService?.sumResultFlow?.collect {
                    Log.d("ISLAM", "Collecting in the viewmodel : $it")

                    Log.d("ISLAM", "the return from the ibinder : $it")

                    _sumResult.value = it
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            helloService = null
        }
    }

    init {
        bindToService()
    }

    private fun bindToService() {
        val intent = Intent(getApplication(), HelloService::class.java)
        getApplication<Application>().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().unbindService(serviceConnection)
    }
}