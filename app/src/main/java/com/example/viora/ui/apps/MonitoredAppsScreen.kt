package com.example.viora.ui.apps

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.viora.domain.repository.SettingsRepository
import com.example.viora.ui.theme.VioraPrimary
import com.example.viora.util.AppInfoHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppsUiState(
    val monitoredPackages: Set<String> = emptySet()
)

@HiltViewModel
class MonitoredAppsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppsUiState())
    val uiState: StateFlow<AppsUiState> = _uiState.asStateFlow()

    val allApps: List<AppInfoHelper.AppInfo> by lazy {
        AppInfoHelper.getInstalledApps(application)
            .filter { it.packageName != application.packageName }
    }

    init {
        viewModelScope.launch {
            settingsRepository.getMonitoredPackages().collect { packages ->
                _uiState.update { it.copy(monitoredPackages = packages) }
            }
        }
    }

    fun togglePackage(packageName: String) {
        viewModelScope.launch {
            val current = _uiState.value.monitoredPackages
            if (packageName in current) {
                settingsRepository.removeMonitoredPackage(packageName)
            } else {
                settingsRepository.addMonitoredPackage(packageName)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitoredAppsScreen(
    viewModel: MonitoredAppsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showAddPicker by remember { mutableStateOf(false) }

    val filteredApps = remember(searchQuery, viewModel.allApps) {
        if (searchQuery.isBlank()) viewModel.allApps
        else viewModel.allApps.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Monitored Apps",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            actions = {
                IconButton(onClick = { showAddPicker = !showAddPicker }) {
                    Icon(
                        imageVector = if (showAddPicker) Icons.Filled.Close else Icons.Filled.Add,
                        contentDescription = if (showAddPicker) "Close" else "Add apps"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        if (showAddPicker) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search apps...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VioraPrimary,
                    cursorColor = VioraPrimary
                )
            )
        }

        if (!showAddPicker && uiState.monitoredPackages.isNotEmpty()) {
            Text(
                text = "Monitoring ${uiState.monitoredPackages.size} apps",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (showAddPicker) {
                items(filteredApps, key = { it.packageName }) { app ->
                    val isMonitored = app.packageName in uiState.monitoredPackages
                    AppPickerRow(
                        appName = app.name,
                        packageName = app.packageName,
                        category = app.category,
                        monitored = isMonitored,
                        onToggle = { viewModel.togglePackage(app.packageName) }
                    )
                }
            } else {
                val monitoredApps = viewModel.allApps.filter {
                    it.packageName in uiState.monitoredPackages
                }

                if (monitoredApps.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 64.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "No apps monitored yet",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "Tap + to add apps you want to track",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(monitoredApps, key = { it.packageName }) { app ->
                        MonitoredAppRow(
                            appName = app.name,
                            packageName = app.packageName,
                            category = app.category,
                            onRemove = { viewModel.togglePackage(app.packageName) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppPickerRow(
    appName: String,
    packageName: String,
    category: String,
    monitored: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = appName,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            )
            Text(
                text = packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = monitored,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(checkedTrackColor = VioraPrimary)
        )
    }
}

@Composable
private fun MonitoredAppRow(
    appName: String,
    packageName: String,
    category: String,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
