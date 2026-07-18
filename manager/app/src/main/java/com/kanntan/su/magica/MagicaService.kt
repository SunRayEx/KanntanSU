package com.kanntan.su.magica

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder

class MagicaService : Service() {
    override fun onBind(intent: Intent): IBinder {
        return Binder()
    }
}
