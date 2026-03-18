# PHASE 6: ProGuard rules for game module

# Keep game models
-keep class com.cascadesim.game.model.** { *; }
-keep class com.cascadesim.game.engine.** { *; }

# Keep Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
