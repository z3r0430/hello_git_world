package com.example.notilog.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.notilog.data.FilterMode
import com.example.notilog.data.FilterSettings
import com.example.notilog.data.NotificationEntry
import java.text.DateFormat
import java.util.Date

@Composable
fun LogScreen(
    entries: List<NotificationEntry>,
    modifier: Modifier = Modifier,
    onDelete: (Long) -> Unit,
) {
    if (entries.isEmpty()) {
        EmptyState(
            title = "No messages yet",
            body = "Captured WhatsApp messages will appear here — and stay here even " +
                "if the sender deletes them in the chat.",
            modifier = modifier,
        )
        return
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(entries, key = { it.id }) { entry ->
            MessageCard(entry, onDelete = { onDelete(entry.id) })
        }
    }
}

@Composable
private fun MessageCard(entry: NotificationEntry, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = entry.sender,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                            .format(Date(entry.timestampMillis)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (!entry.sender.equals(entry.conversation, ignoreCase = true)) {
                    Text(
                        text = "in ${entry.conversation}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text(text = entry.message, style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete entry")
            }
        }
    }
}

@Composable
fun FilterScreen(
    granted: Boolean,
    settings: FilterSettings,
    knownConversations: List<String>,
    modifier: Modifier = Modifier,
    onRequestAccess: () -> Unit,
    onToggleRecording: (Boolean) -> Unit,
    onModeChange: (FilterMode) -> Unit,
    onAddSelected: (String) -> Unit,
    onRemoveSelected: (String) -> Unit,
    onClearLog: () -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Notification access", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (granted) {
                            "Granted. NotiLog can read WhatsApp notifications on this device."
                        } else {
                            "Not granted. NotiLog can't see any notifications until you enable " +
                                "it in system settings."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (!granted) {
                        Spacer(Modifier.width(8.dp))
                        ElevatedButton(onClick = onRequestAccess) {
                            Text("Grant notification access")
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Recording", style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (settings.recordingEnabled) "On" else "Paused",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = settings.recordingEnabled, onCheckedChange = onToggleRecording)
            }
        }

        item {
            Text("Whose chats to record", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.width(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = settings.mode == FilterMode.ALL,
                    onClick = { onModeChange(FilterMode.ALL) },
                    label = { Text("All chats") },
                )
                FilterChip(
                    selected = settings.mode == FilterMode.SELECTED,
                    onClick = { onModeChange(FilterMode.SELECTED) },
                    label = { Text("Selected only") },
                )
            }
        }

        if (settings.mode == FilterMode.SELECTED) {
            item { SelectedEditor(settings, knownConversations, onAddSelected, onRemoveSelected) }
        }

        item {
            HorizontalDivider()
            Spacer(Modifier.width(8.dp))
            OutlinedButton(onClick = onClearLog) { Text("Clear all recorded messages") }
        }
    }
}

@Composable
private fun SelectedEditor(
    settings: FilterSettings,
    knownConversations: List<String>,
    onAddSelected: (String) -> Unit,
    onRemoveSelected: (String) -> Unit,
) {
    var input by remember { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("Add a name or group") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            OutlinedButton(
                onClick = {
                    onAddSelected(input)
                    input = ""
                },
                enabled = input.isNotBlank(),
            ) { Text("Add") }
        }

        if (settings.selected.isEmpty()) {
            Text(
                "Add at least one name — nothing is recorded until you do.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        } else {
            FlowChips(settings.selected.sorted()) { name ->
                InputChip(
                    selected = true,
                    onClick = { onRemoveSelected(name) },
                    label = { Text(name) },
                    trailingIcon = {
                        Icon(Icons.Filled.Delete, contentDescription = "Remove $name")
                    },
                )
            }
        }

        val suggestions = knownConversations.filterNot { known ->
            settings.selected.any { it.equals(known, ignoreCase = true) }
        }
        if (suggestions.isNotEmpty()) {
            Text("Seen recently", style = MaterialTheme.typography.labelMedium)
            FlowChips(suggestions) { name ->
                InputChip(
                    selected = false,
                    onClick = { onAddSelected(name) },
                    label = { Text(name) },
                )
            }
        }
    }
}

/** Minimal wrapping row of chips without extra dependencies. */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun <T> FlowChips(items: List<T>, chip: @Composable (T) -> Unit) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items.forEach { chip(it) }
    }
}

@Composable
private fun EmptyState(title: String, body: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.width(8.dp))
        Text(
            body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
