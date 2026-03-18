package com.cascadesim.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cascadesim.ui.model.UiState

@Composable
fun HomeScreen(
    uiState: UiState,
    onNavigateToDecisions: () -> Unit,
    onNavigateToEvents: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "CascadeSim",
                style = MaterialTheme.typography.headlineLarge
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            when (uiState) {
                is UiState.Loading -> {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Loading simulation...")
                }
                is UiState.Success -> {
                    Text("Simulation Running")
                    Text("Cascade Level: ${uiState.cascadeLevel}")
                }
                is UiState.Error -> {
                    Text("Error: ${uiState.message}")
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onNavigateToDecisions,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Make Decision")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onNavigateToEvents,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Events")
            }
        }
    }
}
