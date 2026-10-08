package com.example.notilog.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.example.notilog.service.WhatsAppNotificationListener

/** Helpers for checking and requesting Notification Access. */
object NotificationAccess {

    fun isGranted(context: Context): Boolean {
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners",
        ) ?: return false
        val component = ComponentName(context, WhatsAppNotificationListener::class.java)
        return enabled.split(":").any {
            val cn = ComponentName.unflattenFromString(it)
            cn == component || cn?.packageName == context.packageName
        }
    }

    fun settingsIntent(): Intent =
        Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}
