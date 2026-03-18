package com.cascadesim

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.cascadesim.core.repository.WorldRepository
import com.cascadesim.navigation.NavGraph
import com.cascadesim.ui.theme.CascadeSimTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var worldRepository: WorldRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // PHASE 6: Schedule background simulation
        worldRepository.scheduleBackgroundSim(this)
        
        setContent {
            CascadeSimTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController)
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // PHASE 6: Cancel background simulation when app is destroyed
        // Note: In production, you might want to keep this running
        // worldRepository.cancelBackgroundSim(this)
    }
}
