# PHASE 6: ProGuard rules for core module

# Keep Room entities and DAOs
-keep class com.cascadesim.core.db.entity.** { *; }
-keep class com.cascadesim.core.db.dao.** { *; }
-keep @androidx.room.Entity class *
-keepclassmembers class * {
    @androidx.room.* <methods>;
}

# Keep Gson models
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.cascadesim.core.** { *; }

# Keep Hilt
-keep class dagger.hilt.** { *; }

# Keep WorkManager
-keep class * extends androidx.work.Worker { *; }
