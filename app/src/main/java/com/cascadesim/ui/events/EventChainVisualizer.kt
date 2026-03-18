// PHASE 5: Event chain visualizer composable with animations

package com.cascadesim.ui.events

import androidx.compose.animation.core.*
import androidx.compose.canvas.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cascadesim.game.model.EventSeverity
import com.cascadesim.game.model.UiEventNode
import com.cascadesim.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Visualizes a chain of cascading events as a vertical timeline.
 * PHASE 5: Added for animated cascade visualization
 */
@Composable
fun EventChainVisualizer(
    events: List<UiEventNode>,
    modifier: Modifier = Modifier
) {
    var visibleNodes by remember { mutableStateOf<List<UiEventNode>>(emptyList()) }
    var targetIndex by remember { mutableStateOf(0) }
    
    LaunchedEffect(events) {
        visibleNodes = emptyList()
        targetIndex = 0
        delay(300)
        
        events.forEachIndexed { index, node ->
            visibleNodes = visibleNodes + node
            targetIndex = index
            delay(400) // Animate each node with delay
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Cascade Chain",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (visibleNodes.isEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                strokeWidth = 2.dp,
                color = CascadePrimary
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                visibleNodes.forEachIndexed { index, node ->
                    EventChainNode(
                        node = node,
                        isRoot = node.isRoot,
                        isLast = index == visibleNodes.lastIndex,
                        showAnimation = index == targetIndex
                    )
                    
                    if (index < visibleNodes.lastIndex) {
                        ConnectorLine(
                            animateIn = index < targetIndex,
                            severity = node.severity
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EventChainNode(
    node: UiEventNode,
    isRoot: Boolean,
    isLast: Boolean,
    showAnimation: Boolean
) {
    val severityColor = when (node.severity) {
        EventSeverity.LOW -> EventSeverityLow
        EventSeverity.MEDIUM -> EventSeverityMedium
        EventSeverity.HIGH -> EventSeverityHigh
        EventSeverity.CRITICAL -> EventSeverityCritical
        EventSeverity.CATASTROPHIC -> EventSeverityCatastrophic
    }
    
    val scale by animateFloatAsState(
        targetValue = if (showAnimation) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (showAnimation) 1f else 0.5f,
        animationSpec = tween(durationMillis = 300)
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = severityColor.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .scale(scale)
                .alpha(alpha),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isRoot) {
                        Badge(
                            containerColor = CascadePrimary,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = "ROOT",
                                fontSize = 10.sp,
                                color = Color.White
                            )
                        }
                    }
                    
                    Text(
                        text = node.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = severityColor
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = node.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun ConnectorLine(
    animateIn: Boolean,
    severity: EventSeverity
) {
    val severityColor = when (severity) {
        EventSeverity.LOW -> EventSeverityLow
        EventSeverity.MEDIUM -> EventSeverityMedium
        EventSeverity.HIGH -> EventSeverityHigh
        EventSeverity.CRITICAL -> EventSeverityCritical
        EventSeverity.CATASTROPHIC -> EventSeverityCatastrophic
    }
    
    val progress by animateFloatAsState(
        targetValue = if (animateIn) 1f else 0f,
        animationSpec = tween(durationMillis = 200)
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (animateIn) 1f else 0.3f,
        animationSpec = tween(durationMillis = 200)
    )
    
    Box(
        modifier = Modifier
            .width(2.dp)
            .height(24.dp)
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val endY = size.height * progress
            
            drawLine(
                color = severityColor.copy(alpha = alpha),
                start = Offset(size.width / 2, 0f),
                end = Offset(size.width / 2, endY),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * Preview for EventChainVisualizer
 */
@androidx.compose.runtime.Composable
@androidx.compose.ui.tooling.preview.Preview
private fun EventChainVisualizerPreview() {
    MaterialTheme {
        Surface {
            EventChainVisualizer(
                events = listOf(
                    UiEventNode(
                        id = "1",
                        label = "HIGH",
                        description = "Economic decision with high impact",
                        level = 0,
                        severity = EventSeverity.HIGH,
                        isRoot = true,
                        timestamp = System.currentTimeMillis()
                    ),
                    UiEventNode(
                        id = "2",
                        label = "MEDIUM",
                        description = "Cascade effect - secondary impact",
                        level = 1,
                        severity = EventSeverity.MEDIUM,
                        isRoot = false,
                        timestamp = System.currentTimeMillis() + 100
                    ),
                    UiEventNode(
                        id = "3",
                        label = "LOW",
                        description = "Tertiary ripple effect",
                        level = 2,
                        severity = EventSeverity.LOW,
                        isRoot = false,
                        timestamp = System.currentTimeMillis() + 200
                    )
                )
            )
        }
    }
}
