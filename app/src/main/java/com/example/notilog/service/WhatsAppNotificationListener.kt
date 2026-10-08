package com.example.notilog.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import com.example.notilog.NotiLogApp
import com.example.notilog.data.NotificationEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Captures WhatsApp notifications and stores their text locally.
 *
 * Android requires the user to enable this service by hand under
 * Settings → Notifications → Notification access; it cannot bind itself.
 */
class WhatsAppNotificationListener : NotificationListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val app get() = applicationContext as NotiLogApp

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName !in WHATSAPP_PACKAGES) return

        val notification = sbn.notification ?: return

        // Skip WhatsApp's own group-summary and foreground/service notifications
        // ("Checking for new messages", "Backup in progress", etc.) — they carry
        // no real message text.
        if (notification.flags and Notification.FLAG_GROUP_SUMMARY != 0) return
        if (Notification.CATEGORY_SERVICE == notification.category) return

        val captured = extractMessages(sbn)
        if (captured.isEmpty()) return

        scope.launch {
            val settings = app.filterStore.settings.first()
            val dao = app.database.notificationDao()
            val now = System.currentTimeMillis()
            for (msg in captured) {
                if (!settings.shouldRecord(msg.conversation, msg.sender)) continue
                dao.insert(
                    NotificationEntry(
                        packageName = sbn.packageName,
                        conversation = msg.conversation,
                        sender = msg.sender,
                        message = msg.text,
                        timestampMillis = msg.timestampMillis,
                        capturedAtMillis = now,
                    ),
                )
            }
        }
    }

    private data class CapturedMessage(
        val conversation: String,
        val sender: String,
        val text: String,
        val timestampMillis: Long,
    )

    /**
     * Prefers the structured MessagingStyle payload so each message is captured
     * exactly once. Falls back to the plain title/text extras for notifications
     * that don't use MessagingStyle.
     */
    private fun extractMessages(sbn: StatusBarNotification): List<CapturedMessage> {
        val notification = sbn.notification
        val extras = notification.extras ?: return emptyList()
        val conversation =
            extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim().orEmpty()

        val style = NotificationCompat.MessagingStyle
            .extractMessagingStyleFromNotification(notification)
        if (style != null && style.messages.isNotEmpty()) {
            val chatTitle = style.conversationTitle?.toString()?.trim()?.takeIf { it.isNotEmpty() }
                ?: conversation
            return style.messages.mapNotNull { m ->
                val text = m.text?.toString()?.trim().orEmpty()
                if (text.isEmpty()) return@mapNotNull null
                val sender = m.person?.name?.toString()?.trim()?.takeIf { it.isNotEmpty() }
                    ?: chatTitle.ifEmpty { UNKNOWN }
                CapturedMessage(
                    conversation = chatTitle.ifEmpty { sender },
                    sender = sender,
                    text = text,
                    timestampMillis = if (m.timestamp > 0) m.timestamp else sbn.postTime,
                )
            }
        }

        // Fallback: single plain-text message.
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim().orEmpty()
        if (text.isEmpty()) return emptyList()
        val title = conversation.ifEmpty { UNKNOWN }
        return listOf(
            CapturedMessage(
                conversation = title,
                sender = title,
                text = text,
                timestampMillis = sbn.postTime,
            ),
        )
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private val WHATSAPP_PACKAGES = setOf("com.whatsapp", "com.whatsapp.w4b")
        private const val UNKNOWN = "Unknown"
    }
}
