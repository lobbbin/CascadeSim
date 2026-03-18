package com.cascadesim.ui.events

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cascadesim.common.model.UiEventNode

@Composable
fun EventChainVisualizer(
    events: List<UiEventNode>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        if (events.isEmpty()) {
            Text("No events to display")
        } else {
            events.forEach { event ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(event.description)
                    }
                }
            }
        }
    }
}
