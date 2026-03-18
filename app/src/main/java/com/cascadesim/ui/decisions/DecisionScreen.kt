package com.cascadesim.ui.decisions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cascadesim.MainActivityViewModel
import com.cascadesim.game.model.Decision
import com.cascadesim.game.model.DecisionType
import com.cascadesim.ui.model.UiState
import com.cascadesim.ui.theme.CascadePrimary
import com.cascadesim.ui.theme.CrisisRed
import com.cascadesim.ui.theme.StabilityGreen
import com.cascadesim.ui.theme.WarningOrange
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun DecisionScreen(
    onNavigateBack: () -> Unit,
    viewModel: MainActivityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Decisions") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
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
                DecisionContent(
                    isLoading = false,
                    onDecisionMade = { decision ->
                        scope.launch {
                            viewModel.onDecisionMade(decision)
                        }
                    },
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
private fun DecisionContent(
    isLoading: Boolean,
    onDecisionMade: (Decision) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Available Policies",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Diplomatic Decisions
        item {
            SectionHeader(title = "Diplomatic")
        }
        items(getDiplomaticDecisions()) { decision ->
            DecisionCard(
                decision = decision,
                enabled = !isLoading,
                onClick = { onDecisionMade(decision) }
            )
        }
        
        // Economic Decisions
        item {
            SectionHeader(title = "Economic")
        }
        items(getEconomicDecisions()) { decision ->
            DecisionCard(
                decision = decision,
                enabled = !isLoading,
                onClick = { onDecisionMade(decision) }
            )
        }
        
        // Military Decisions
        item {
            SectionHeader(title = "Military")
        }
        items(getMilitaryDecisions()) { decision ->
            DecisionCard(
                decision = decision,
                enabled = !isLoading,
                onClick = { onDecisionMade(decision) }
            )
        }
        
        // Social Decisions
        item {
            SectionHeader(title = "Social")
        }
        items(getSocialDecisions()) { decision ->
            DecisionCard(
                decision = decision,
                enabled = !isLoading,
                onClick = { onDecisionMade(decision) }
            )
        }
        
        // Environmental Decisions
        item {
            SectionHeader(title = "Environmental")
        }
        items(getEnvironmentalDecisions()) { decision ->
            DecisionCard(
                decision = decision,
                enabled = !isLoading,
                onClick = { onDecisionMade(decision) }
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun DecisionCard(
    decision: Decision,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val impactColor = when {
        decision.impactScore >= 0.7f -> CrisisRed
        decision.impactScore >= 0.4f -> WarningOrange
        else -> StabilityGreen
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant
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
                Text(
                    text = decision.id,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                ImpactBadge(impactScore = decision.impactScore, color = impactColor)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Type: ${decision.type.name}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (decision.metadata.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                decision.metadata.forEach { (key, value) ->
                    Text(
                        text = "$key: $value",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ImpactBadge(
    impactScore: Float,
    color: Color
) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = "${(impactScore * 100).toInt()}% Impact",
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// Sample decision generators
private fun getDiplomaticDecisions(): List<Decision> = listOf(
    Decision(
        id = "Trade Agreement",
        type = DecisionType.DIPLOMATIC,
        impactScore = 0.4f
    ),
    Decision(
        id = "Alliance Proposal",
        type = DecisionType.DIPLOMATIC,
        impactScore = 0.6f
    ),
    Decision(
        id = "Sanctions",
        type = DecisionType.DIPLOMATIC,
        impactScore = 0.7f
    )
)

private fun getEconomicDecisions(): List<Decision> = listOf(
    Decision(
        id = "Tax Reform",
        type = DecisionType.ECONOMIC,
        impactScore = 0.5f
    ),
    Decision(
        id = "Infrastructure Investment",
        type = DecisionType.ECONOMIC,
        impactScore = 0.6f
    ),
    Decision(
        id = "Austerity Measures",
        type = DecisionType.ECONOMIC,
        impactScore = 0.8f
    )
)

private fun getMilitaryDecisions(): List<Decision> = listOf(
    Decision(
        id = "Defense Budget Increase",
        type = DecisionType.MILITARY,
        impactScore = 0.5f
    ),
    Decision(
        id = "Military Exercise",
        type = DecisionType.MILITARY,
        impactScore = 0.4f
    ),
    Decision(
        id = "Intervention",
        type = DecisionType.MILITARY,
        impactScore = 0.9f
    )
)

private fun getSocialDecisions(): List<Decision> = listOf(
    Decision(
        id = "Education Reform",
        type = DecisionType.SOCIAL,
        impactScore = 0.5f
    ),
    Decision(
        id = "Healthcare Expansion",
        type = DecisionType.SOCIAL,
        impactScore = 0.6f
    ),
    Decision(
        id = "Welfare Program",
        type = DecisionType.SOCIAL,
        impactScore = 0.4f
    )
)

private fun getEnvironmentalDecisions(): List<Decision> = listOf(
    Decision(
        id = "Carbon Tax",
        type = DecisionType.ENVIRONMENTAL,
        impactScore = 0.6f
    ),
    Decision(
        id = "Renewable Energy Subsidy",
        type = DecisionType.ENVIRONMENTAL,
        impactScore = 0.5f
    ),
    Decision(
        id = "Conservation Act",
        type = DecisionType.ENVIRONMENTAL,
        impactScore = 0.4f
    )
)
