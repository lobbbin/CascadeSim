package com.cascadesim.ui.events

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cascadesim.MainActivityViewModel
import com.cascadesim.ui.model.EventUiModel
import com.cascadesim.ui.model.UiState

@Composable
fun EventFeedScreen(
    onNavigateBack: () -> Unit,
    viewModel: MainActivityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Events") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        val events = when (val state = uiState) {
            is UiState.Success -> state.recentEvents
            else -> emptyList()
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            items(events) { event ->
                EventListItem(event = event)
            }
        }
    }
}

@Composable
fun EventListItem(event: EventUiModel, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Severity: ${event.severity.name}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
