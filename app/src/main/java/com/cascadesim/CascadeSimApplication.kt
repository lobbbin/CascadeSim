// PHASE 6: Updated Application class with WorkManager initialization

package com.cascadesim

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application class for CascadeSim.
 * Required for Hilt dependency injection.
 * 
 * PHASE 6: Added WorkManager configuration for background simulation
 */
@HiltAndroidApp
class CascadeSimApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // Note: Background simulation is scheduled from MainActivity
        // to ensure proper lifecycle management
    }
}
