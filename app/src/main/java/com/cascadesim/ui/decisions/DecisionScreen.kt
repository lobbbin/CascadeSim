package com.cascadesim.ui.decisions

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cascadesim.MainActivityViewModel
import com.cascadesim.common.model.Decision
import com.cascadesim.common.model.DecisionType
import com.cascadesim.ui.model.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecisionScreen(
    onNavigateBack: () -> Unit,
    viewModel: MainActivityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Decisions") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Choose a decision type:",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            DecisionType.values().take(4).forEach { type ->
                Button(
                    onClick = {
                        viewModel.onDecisionMade(
                            Decision(
                                id = "decision_${type.name}_${System.currentTimeMillis()}",
                                type = type,
                                impactScore = 0.5f
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(type.name)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { viewModel.onTick() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Tick")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { viewModel.onReset() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Reset Simulation")
            }
        }
    }
}
