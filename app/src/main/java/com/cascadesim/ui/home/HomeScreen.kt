package com.cascadesim.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cascadesim.MainActivityViewModel
import com.cascadesim.ui.model.CascadeLevel
import com.cascadesim.ui.model.CountryUiModel
import com.cascadesim.ui.model.EventUiModel
import com.cascadesim.ui.model.UiState
import com.cascadesim.ui.theme.*
import kotlinx.coroutines.flow.collectLatest

@Composable
fun HomeScreen(
    onNavigateToDecisions: () -> Unit,
    onNavigateToEvents: () -> Unit,
    viewModel: MainActivityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    when (val state = uiState) {
        is UiState.Loading -> {
            LoadingScreen()
        }
        is UiState.Success -> {
            HomeContent(
                cascadeLevel = state.cascadeLevel,
                countries = emptyList(), // TODO: Populate from repository
                recentEvents = emptyList(), // TODO: Populate from repository
                onNavigateToDecisions = onNavigateToDecisions,
                onNavigateToEvents = onNavigateToEvents
            )
        }
        is UiState.Error -> {
            ErrorScreen(
                message = state.message,
                onRetry = { viewModel.clearError() }
            )
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = CascadePrimary)
    }
}

@Composable
private fun ErrorScreen(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Error",
                tint = CrisisRed,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge
            )
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun HomeContent(
    cascadeLevel: CascadeLevel,
    countries: List<CountryUiModel>,
    recentEvents: List<EventUiModel>,
    onNavigateToDecisions: () -> Unit,
    onNavigateToEvents: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Text(
                text = "CascadeSim",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Cascade Level Indicator
        item {
            CascadeLevelCard(cascadeLevel)
        }
        
        // Country Stats
        item {
            CountryStatsCard(countries)
        }
        
        // Recent Events Preview
        item {
            RecentEventsPreview(
                events = recentEvents,
                onViewAll = onNavigateToEvents
            )
        }
        
        // Navigation Buttons
        item {
            NavigationButtons(
                onNavigateToDecisions = onNavigateToDecisions,
                onNavigateToEvents = onNavigateToEvents
            )
        }
    }
}

@Composable
private fun CascadeLevelCard(cascadeLevel: CascadeLevel) {
    val (color, label) = when (cascadeLevel) {
        CascadeLevel.STABLE -> StabilityGreen to "Stable"
        CascadeLevel.UNSTABLE -> WarningOrange to "Unstable"
        CascadeLevel.CRITICAL -> CrisisRed to "Critical"
        CascadeLevel.CASCADE -> CascadeLevelCascade to "Cascade"
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Cascade Level",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = color,
                        strokeWidth = 3.dp,
                        progress = 0.75f
                    )
                }
            }
        }
    }
}

@Composable
private fun CountryStatsCard(countries: List<CountryUiModel>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Country Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            if (countries.isEmpty()) {
                Text(
                    text = "No countries loaded",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                countries.take(3).forEach { country ->
                    CountryStatRow(country)
                }
            }
        }
    }
}

@Composable
private fun CountryStatRow(country: CountryUiModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = country.name,
            style = MaterialTheme.typography.bodyMedium
        )
        LinearProgressIndicator(
            progress = country.stability,
            modifier = Modifier
                .width(100.dp)
                .height(8.dp),
            color = if (country.stability > 0.5f) StabilityGreen else CrisisRed,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Text(
            text = "${country.stabilityPercent}%",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun RecentEventsPreview(
    events: List<EventUiModel>,
    onViewAll: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
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
                Text(
                    text = "Recent Events",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onViewAll) {
                    Text("View All")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            if (events.isEmpty()) {
                Text(
                    text = "No events yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                events.take(3).forEach { event ->
                    EventPreviewRow(event)
                }
            }
        }
    }
}

@Composable
private fun EventPreviewRow(event: EventUiModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = event.description,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .size(12.dp)
                .padding(start = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp),
                color = if (event.isHighSeverity) CrisisRed else StabilityGreen
            )
        }
    }
}

@Composable
private fun NavigationButtons(
    onNavigateToDecisions: () -> Unit,
    onNavigateToEvents: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onNavigateToDecisions,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Make Decisions")
        }
        OutlinedButton(
            onClick = onNavigateToEvents,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View Event Feed")
        }
    }
}
