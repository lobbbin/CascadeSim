package com.cascadesim.di

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.cascadesim.common.work.WorkConstants
import com.cascadesim.game.work.SimulationWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper for managing background simulation work.
 * Lives in :app module to avoid circular dependencies.
 */
@Singleton
class SimulationWorkManager @Inject constructor() {

    fun scheduleBackgroundSim(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(false)
            .setRequiresCharging(false)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<SimulationWorker>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag(WorkConstants.WORK_NAME)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WorkConstants.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    fun cancelBackgroundSim(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WorkConstants.WORK_NAME)
    }
}
