# ProGuard rules for common module
# Keep shared entities and models
-keep class com.cascadesim.common.** { *; }
-keep @androidx.room.Entity class *
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
