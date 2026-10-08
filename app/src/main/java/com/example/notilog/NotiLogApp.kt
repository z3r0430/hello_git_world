package com.example.notilog

import android.app.Application
import com.example.notilog.data.AppDatabase
import com.example.notilog.data.FilterSettingsStore

/** Holds process-wide singletons so the service and UI share one database. */
class NotiLogApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.get(this) }
    val filterStore: FilterSettingsStore by lazy { FilterSettingsStore(this) }
}
