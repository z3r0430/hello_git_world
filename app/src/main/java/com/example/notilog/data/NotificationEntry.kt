package com.example.notilog.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One captured message. Text only.
 *
 * [timestampMillis] is the message's own timestamp as reported by WhatsApp's
 * MessagingStyle payload (not the time we stored it), which makes the unique
 * index a reliable de-duplicator: WhatsApp re-posts the same notification as a
 * thread grows, but each underlying message keeps a stable timestamp.
 */
@Entity(
    tableName = "notifications",
    indices = [
        Index(
            value = ["packageName", "conversation", "sender", "message", "timestampMillis"],
            unique = true,
        ),
        Index(value = ["timestampMillis"]),
    ],
)
data class NotificationEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    /** Chat title: a person's name, or a group name. */
    val conversation: String,
    /** Who sent the message. For 1:1 chats this equals [conversation]. */
    val sender: String,
    val message: String,
    /** Message time reported by WhatsApp, epoch millis. */
    val timestampMillis: Long,
    /** When we recorded it, epoch millis. */
    val capturedAtMillis: Long,
)
