package com.cascadesim.core.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Dependency injection module for CascadeSim core functionality.
 * Core module doesn't provide any bindings - all dependencies come from :app.
 */
@Module
@InstallIn(SingletonComponent::class)
object CoreModule {
    // No bindings - Gson is provided by GameEngineModule in :app
}
