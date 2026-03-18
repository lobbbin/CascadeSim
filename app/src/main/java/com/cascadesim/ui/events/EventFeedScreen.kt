package com.cascadesim.ui.events

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cascadesim.MainActivityViewModel
import com.cascadesim.game.model.EventSeverity
import com.cascadesim.ui.model.EventUiModel
import com.cascadesim.ui.model.UiState
import com.cascadesim.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EventFeedScreen(
    onNavigateBack: () -> Unit,
    viewModel: MainActivityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Feed") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        // Scroll to top
                        listState.animateScrollToItem(0)
                    }) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Scroll to newest"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is UiState.Success -> {
                EventFeedContent(
                    events = state.recentEvents,
                    listState = listState,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = CrisisRed
                    )
                }
            }
        }
    }
}

@Composable
private fun EventFeedContent(
    events: List<EventUiModel>,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "All Events",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                EventCountBadge(count = events.size)
            }
        }
        
        if (events.isEmpty()) {
            item {
                EmptyEventsMessage()
            }
        } else {
            items(events, key = { it.id }) { event ->
                EventCard(event = event)
            }
        }
    }
}

@Composable
private fun EventCountBadge(count: Int) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = "$count events",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun EmptyEventsMessage() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "No events yet",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Make decisions to see cascade effects",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EventCard(event: EventUiModel) {
    val severityColor = when (event.severity) {
        EventSeverity.LOW -> EventSeverityLow
        EventSeverity.MEDIUM -> EventSeverityMedium
        EventSeverity.HIGH -> EventSeverityHigh
        EventSeverity.CRITICAL -> EventSeverityCritical
        EventSeverity.CATASTROPHIC -> EventSeverityCatastrophic
    }
    
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val timeString = timeFormat.format(Date(event.timestamp))
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (event.isHighSeverity) {
                severityColor.copy(alpha = 0.05f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SeverityBadge(severity = event.severity, color = severityColor)
                Text(
                    text = timeString,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyLarge,
                color = if (event.isHighSeverity) {
                    severityColor
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Chain: ${event.chainId.take(8)}...",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (event.isHighSeverity) {
                    Surface(
                        color = severityColor.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.extraSmall
                    ) {
                        Text(
                            text = "HIGH IMPACT",
                            style = MaterialTheme.typography.labelSmall,
                            color = severityColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SeverityBadge(severity: EventSeverity, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = severity.name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
