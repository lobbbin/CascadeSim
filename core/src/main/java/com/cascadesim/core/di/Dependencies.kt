package com.cascadesim.core.di

import com.cascadesim.game.engine.CascadeEngine
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dependency injection module for CascadeSim core functionality.
 * Provides singleton dependencies across the application.
 */
@Module
@InstallIn(SingletonComponent::class)
object Dependencies {
    
    @Provides
    @Singleton
    fun provideCascadeEngine(): CascadeEngine {
        return CascadeEngine()
    }
    
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return Gson()
    }
}
