package com.kanntan.su.magica

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import me.weishu.kernelsu.Natives

class BootCompletedReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "KanntanSU"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return

        val action = intent.action
        if (action != Intent.ACTION_LOCKED_BOOT_COMPLETED &&
            action != Intent.ACTION_BOOT_COMPLETED &&
            action != "com.kanntan.su.magica.LAUNCH"
        ) {
            return
        }

        if (Natives.isSuEnabled()) return

        try {
            context.startService(Intent(context, MagicaService::class.java))
            Log.i(TAG, "MagicaService started from boot action: $action")
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to start MagicaService from boot action: $action", e)
        }
    }

    fun setEnabled(context: Context, enabled: Boolean) {
        context.packageManager.setComponentEnabledSetting(
            ComponentName(context, BootCompletedReceiver::class.java),
            if (enabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }
}
