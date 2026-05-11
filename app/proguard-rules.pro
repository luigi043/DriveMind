# DriveMind ProGuard Rules

# Keep domain models (Room + Parcelize)
-keep class com.drivemind.domain.model.** { *; }
-keep class com.drivemind.data.local.entity.** { *; }

# Hilt
-keepnames @dagger.hilt.android.lifecycle.HiltViewModel class * extends androidx.lifecycle.ViewModel

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers @androidx.room.Entity class * { *; }

# Navigation SafeArgs
-keepnames class com.drivemind.ui.** { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
