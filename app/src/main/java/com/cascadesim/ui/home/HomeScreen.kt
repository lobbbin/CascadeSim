// PHASE 5: HomeScreen with animated cards, pull-to-refresh, and cascade activity sparkline

package com.cascadesim.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cascadesim.MainActivityViewModel
import com.cascadesim.ui.model.CascadeLevel
import com.cascadesim.ui.model.CountryUiModel
import com.cascadesim.ui.model.EventUiModel
import com.cascadesim.ui.model.UiState
import com.cascadesim.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    onNavigateToDecisions: () -> Unit,
    onNavigateToEvents: () -> Unit,
    viewModel: MainActivityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }
    
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            viewModel.onTick()
            delay(500)
            isRefreshing = false
        }
    }
    
    when (val state = uiState) {
        is UiState.Loading -> {
            LoadingScreen()
        }
        is UiState.Success -> {
            PullRefreshIndicatorWrapper(
                isRefreshing = isRefreshing,
                onRefresh = { isRefreshing = true }
            ) {
                HomeContent(
                    cascadeLevel = state.cascadeLevel,
                    countries = state.countries,
                    recentEvents = state.recentEvents,
                    worldState = state.worldState,
                    onNavigateToDecisions = onNavigateToDecisions,
                    onNavigateToEvents = onNavigateToEvents
                )
            }
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
    worldState: com.cascadesim.game.model.WorldState,
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
        
        // PHASE 5: Cascade Activity Sparkline
        item {
            CascadeActivityChart(tickCount = worldState.tickCount)
        }
        
        // Cascade Level Indicator
        item {
            AnimatedCascadeLevelCard(cascadeLevel)
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

/**
 * PHASE 5: Sparkline chart showing cascade activity over time
 */
@Composable
private fun CascadeActivityChart(
    tickCount: Long,
    modifier: Modifier = Modifier
) {
    // Generate sample data points based on tick count
    val dataPoints = remember(tickCount) {
        List(10) { i ->
            ((i + 1) * 0.1 + (tickCount % 10) * 0.05).toFloat().coerceIn(0.1f, 0.9f)
        }
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = CascadePrimary.copy(alpha = 0.05f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Cascade Activity",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Tick: $tickCount",
                    style = MaterialTheme.typography.labelSmall,
                    color = CascadePrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Sparkline
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                SparklineChart(
                    dataPoints = dataPoints,
                    color = CascadePrimary
                )
            }
        }
    }
}

@Composable
private fun SparklineChart(
    dataPoints: List<Float>,
    color: Color
) {
    androidx.compose.canvas.Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val width = size.width
        val height = size.height
        val padding = 8.dp.toPx()
        
        val chartWidth = width - (padding * 2)
        val chartHeight = height - (padding * 2)
        
        if (dataPoints.size < 2) return@Canvas
        
        val path = Path().apply {
            val stepX = chartWidth / (dataPoints.size - 1)
            
            moveTo(
                padding,
                padding + chartHeight * (1 - dataPoints[0])
            )
            
            for (i in 1 until dataPoints.size) {
                val x = padding + (i * stepX)
                val y = padding + chartHeight * (1 - dataPoints[i])
                lineTo(x, y)
            }
        }
        
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
        
        // Draw data points
        val stepX = chartWidth / (dataPoints.size - 1)
        dataPoints.forEachIndexed { i, value ->
            val x = padding + (i * stepX)
            val y = padding + chartHeight * (1 - value)
            
            drawCircle(
                color = color,
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}

@Composable
private fun AnimatedCascadeLevelCard(cascadeLevel: CascadeLevel) {
    val (color, label) = when (cascadeLevel) {
        CascadeLevel.STABLE -> StabilityGreen to "Stable"
        CascadeLevel.UNSTABLE -> WarningOrange to "Unstable"
        CascadeLevel.CRITICAL -> CrisisRed to "Critical"
        CascadeLevel.CASCADE -> CascadeLevelCascade to "Cascade"
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "cascade")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = alpha)
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
                    AnimatedCountryStatRow(country)
                }
            }
        }
    }
}

/**
 * PHASE 5: Animated country stat row with size animation
 */
@Composable
private fun AnimatedCountryStatRow(country: CountryUiModel) {
    var targetStability by remember { mutableStateOf(country.stability) }
    
    LaunchedEffect(country.stability) {
        targetStability = country.stability
    }
    
    val animatedStability by animateFloatAsState(
        targetValue = targetStability,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "stability"
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = country.name,
            style = MaterialTheme.typography.bodyMedium
        )
        
        // Animated progress bar
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(8.dp)
        ) {
            LinearProgressIndicator(
                progress = animatedStability,
                modifier = Modifier
                    .fillMaxSize()
                    .height(8.dp),
                color = if (animatedStability > 0.5f) StabilityGreen else CrisisRed,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
        
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

/**
 * PHASE 5: Pull-to-refresh wrapper
 */
@Composable
private fun PullRefreshIndicatorWrapper(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    content: @Composable () -> Unit
) {
    Box {
        content()
        
        // Simple refresh indicator
        if (isRefreshing) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = CascadePrimary
                )
            }
        }
    }
}
