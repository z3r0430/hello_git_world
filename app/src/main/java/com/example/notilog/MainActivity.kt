package com.example.notilog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notilog.ui.FilterScreen
import com.example.notilog.ui.LogScreen
import com.example.notilog.ui.MainViewModel
import com.example.notilog.ui.NotiLogTheme
import com.example.notilog.ui.NotificationAccess

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        val app = application as NotiLogApp
        MainViewModel.Factory(app.database, app.filterStore)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NotiLogTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    NotiLogApp(viewModel)
                }
            }
        }
    }
}

private enum class Tab(val label: String) { LOG("Log"), FILTER("Filter") }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotiLogApp(viewModel: MainViewModel) {
    val context = LocalContext.current
    var tab by rememberSaveable { mutableStateOf(Tab.LOG) }

    // Re-check notification-access state whenever we come back to the foreground
    // (the user may have toggled it in system settings).
    var granted by remember { mutableStateOf(NotificationAccess.isGranted(context)) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                granted = NotificationAccess.isGranted(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val entries by viewModel.entries.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val conversations by viewModel.knownConversations.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("NotiLog") }) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == Tab.LOG,
                    onClick = { tab = Tab.LOG },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    label = { Text(Tab.LOG.label) },
                )
                NavigationBarItem(
                    selected = tab == Tab.FILTER,
                    onClick = { tab = Tab.FILTER },
                    icon = { Icon(Icons.Filled.Tune, contentDescription = null) },
                    label = { Text(Tab.FILTER.label) },
                )
            }
        },
    ) { inner ->
        when (tab) {
            Tab.LOG -> LogScreen(
                entries = entries,
                modifier = Modifier.padding(inner),
                onDelete = viewModel::deleteEntry,
            )
            Tab.FILTER -> FilterScreen(
                granted = granted,
                settings = settings,
                knownConversations = conversations,
                modifier = Modifier.padding(inner),
                onRequestAccess = { context.startActivity(NotificationAccess.settingsIntent()) },
                onToggleRecording = viewModel::setRecordingEnabled,
                onModeChange = viewModel::setMode,
                onAddSelected = viewModel::addSelected,
                onRemoveSelected = viewModel::removeSelected,
                onClearLog = viewModel::clearLog,
            )
        }
    }
}
