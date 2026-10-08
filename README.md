# NotiLog — WhatsApp Notification Recorder

A small Android app that keeps a **local, text-only log of WhatsApp notifications
that arrive on your own phone**. Because WhatsApp delivers each message as a
notification before a sender can delete it, a notification log lets you re-read a
message that was removed from the chat.

This is the same mechanism used by Android's own "Notification history" and by
published apps in this category. It does nothing you can't already see on your
device — it just keeps a copy.

## What it does

- Listens for notifications from `com.whatsapp` and `com.whatsapp.w4b`
  (WhatsApp + WhatsApp Business) using `NotificationListenerService`.
- Parses the `MessagingStyle` payload so each individual message (sender, text,
  timestamp) is stored once, instead of the concatenated "3 new messages" blob.
- Stores entries in a local Room database. **Text only** — no media.
- Lets you choose *whose* chats to record: **All chats**, or a **selected list**
  of senders / group names.
- Can be paused at any time.

## What it deliberately does NOT do

- **No `INTERNET` permission.** The app cannot send your data anywhere. Open
  `AndroidManifest.xml` to verify — there is no network permission, so nothing
  leaves the device.
- No media, no call recording, no screen capture.
- It cannot run silently or be installed covertly: Android forces *you* to grant
  **Settings → Notifications → Notification access** by hand, and the app appears
  normally in your launcher and settings.

## Intended use & responsibility

Install this on **a phone you own and control**, to keep your own notifications.
Logging notifications that belong to other people, or putting this on someone
else's device without their knowledge, is a privacy violation and is illegal in
many places (wiretapping / interception and data-protection laws). That's on you,
not the tool.

## Building

Open the project in Android Studio (Koala / 2024.1 or newer) and let it sync, or
from the command line:

```bash
gradle wrapper        # first time, if the wrapper jar isn't present
./gradlew assembleDebug
```

The APK lands in `app/build/outputs/apk/debug/`.

### First run

1. Launch **NotiLog**.
2. Tap **Grant notification access** and enable NotiLog in the system list.
3. Choose **All chats** or add specific senders under **Filter**.
4. Incoming WhatsApp messages now appear in the **Log** tab, and stay there even
   if the sender deletes them in WhatsApp.

## Tech

Kotlin · Jetpack Compose (Material 3) · Room · DataStore · `minSdk 24` / `targetSdk 34`.
