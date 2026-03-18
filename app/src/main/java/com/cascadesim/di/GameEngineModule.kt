package com.cascadesim.di

import com.cascadesim.game.engine.CascadeEngine
import com.cascadesim.game.engine.NpcReactor
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dependency injection module for CascadeSim game engine.
 * Lives in :app module to avoid circular dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object GameEngineModule {

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
