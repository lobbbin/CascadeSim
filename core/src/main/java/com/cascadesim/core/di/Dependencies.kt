// FIX: PHASE 4 - Added Gson provider
// PHASE 4: Added NpcReactor provider

package com.cascadesim.core.di

import com.cascadesim.game.engine.CascadeEngine
import com.cascadesim.game.engine.NpcReactor
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dependency injection module for CascadeSim core functionality.
 * Provides singleton dependencies across the application.
 * 
 * PHASE 4: Added NpcReactor and Gson providers
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

    @Provides
    @Singleton
    fun provideNpcReactor(): NpcReactor {
        return NpcReactor()
    }
}
