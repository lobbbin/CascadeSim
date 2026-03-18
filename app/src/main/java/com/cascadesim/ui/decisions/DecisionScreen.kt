// PHASE 5: Interactive DecisionScreen with haptic feedback, draggable cards, and impact preview

package com.cascadesim.ui.decisions

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.HapticFeedbackType
import androidx.hilt.navigation.compose.hiltViewModel
import com.cascadesim.MainActivityViewModel
import com.cascadesim.common.model.Decision
import com.cascadesim.common.model.DecisionType
import com.cascadesim.ui.model.UiState
import com.cascadesim.ui.theme.*
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.abs

@Composable
fun DecisionScreen(
    onNavigateBack: () -> Unit,
    viewModel: MainActivityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    
    var showImpactPreview by remember { mutableStateOf<Decision?>(null) }
    
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
                    CircularProgressIndicator(color = CascadePrimary)
                }
            }
            is UiState.Success, is UiState.Error -> {
                DecisionContent(
                    isLoading = state is UiState.Loading,
                    onDecisionSelected = { decision ->
                        showImpactPreview = decision
                    },
                    onDecisionConfirmed = { decision ->
                        scope.launch {
                            viewModel.onDecisionMade(decision)
                        }
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
    
    // PHASE 5: Impact Preview Dialog
    showImpactPreview?.let { decision ->
        DecisionImpactPreviewDialog(
            decision = decision,
            onConfirm = {
                viewModel.onDecisionMade(decision)
                showImpactPreview = null
            },
            onDismiss = {
                showImpactPreview = null
            }
        )
    }
}

@Composable
private fun DecisionContent(
    isLoading: Boolean,
    onDecisionSelected: (Decision) -> Unit,
    onDecisionConfirmed: (Decision) -> Unit,
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
            InteractiveDecisionCard(
                decision = decision,
                enabled = !isLoading,
                onClick = { onDecisionSelected(decision) }
            )
        }
        
        // Economic Decisions
        item {
            SectionHeader(title = "Economic")
        }
        items(getEconomicDecisions()) { decision ->
            InteractiveDecisionCard(
                decision = decision,
                enabled = !isLoading,
                onClick = { onDecisionSelected(decision) }
            )
        }
        
        // Military Decisions
        item {
            SectionHeader(title = "Military")
        }
        items(getMilitaryDecisions()) { decision ->
            InteractiveDecisionCard(
                decision = decision,
                enabled = !isLoading,
                onClick = { onDecisionSelected(decision) }
            )
        }
        
        // Social Decisions
        item {
            SectionHeader(title = "Social")
        }
        items(getSocialDecisions()) { decision ->
            InteractiveDecisionCard(
                decision = decision,
                enabled = !isLoading,
                onClick = { onDecisionSelected(decision) }
            )
        }
        
        // Environmental Decisions
        item {
            SectionHeader(title = "Environmental")
        }
        items(getEnvironmentalDecisions()) { decision ->
            InteractiveDecisionCard(
                decision = decision,
                enabled = !isLoading,
                onClick = { onDecisionSelected(decision) }
            )
        }
    }
}

/**
 * PHASE 5: Interactive decision card with swipe gesture and haptic feedback
 */
@Composable
private fun InteractiveDecisionCard(
    decision: Decision,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    var offsetX by remember { mutableStateOf(0f) }
    var isSwiped by remember { mutableStateOf(false) }
    
    val impactColor = when {
        decision.impactScore >= 0.7f -> CrisisRed
        decision.impactScore >= 0.4f -> WarningOrange
        else -> StabilityGreen
    }
    
    val cardModifier = Modifier
        .fillMaxWidth()
        .graphicsLayer {
            translationX = offsetX
            rotationZ = offsetX * 0.05f
        }
        .pointerInput(Unit) {
            detectHorizontalDragGestures(
                onDragEnd = {
                    if (abs(offsetX) > 100f && enabled) {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onClick()
                    }
                    offsetX = 0f
                    isSwiped = false
                },
                onHorizontalDrag = { change, dragAmount ->
                    if (enabled) {
                        offsetX += dragAmount
                        isSwiped = abs(offsetX) > 50f
                    }
                }
            )
        }
        .animateOffset()
    
    Card(
        modifier = cardModifier,
        enabled = enabled,
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
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // PHASE 5: Swipe hint
            AnimatedVisibility(
                visible = !isSwiped && enabled,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Swipe or tap to preview impact",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (decision.metadata.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
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

/**
 * PHASE 5: Impact preview dialog showing predicted effects
 */
@Composable
private fun DecisionImpactPreviewDialog(
    decision: Decision,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Confirm Decision",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Decision info
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = decision.id,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Type",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = decision.type.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Impact Score",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${(decision.impactScore * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = when {
                                    decision.impactScore >= 0.7f -> CrisisRed
                                    decision.impactScore >= 0.4f -> WarningOrange
                                    else -> StabilityGreen
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // PHASE 5: Predicted effects
                Text(
                    text = "Predicted Effects",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = CascadePrimary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                PredictedEffectsList(decision = decision)
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            onConfirm()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CascadePrimary
                        )
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}

@Composable
private fun PredictedEffectsList(decision: Decision) {
    val effects = when (decision.type) {
        DecisionType.DIPLOMATIC -> listOf(
            "International relations affected",
            "Trade agreements may change",
            "Alliance shifts possible"
        )
        DecisionType.ECONOMIC -> listOf(
            "GDP impact expected",
            "Employment rates may shift",
            "Market volatility possible"
        )
        DecisionType.MILITARY -> listOf(
            "Defense posture changed",
            "Regional tension may increase",
            "Resource allocation required"
        )
        DecisionType.SOCIAL -> listOf(
            "Public opinion affected",
            "Social services impacted",
            "Community response expected"
        )
        DecisionType.ENVIRONMENTAL -> listOf(
            "Ecological impact expected",
            "Resource consumption affected",
            "Long-term sustainability change"
        )
        DecisionType.EMERGENCY -> listOf(
            "Immediate response required",
            "Crisis management activated",
            "Resource reallocation needed"
        )
    }
    
    effects.forEach { effect ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(CascadePrimary)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = effect,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
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
