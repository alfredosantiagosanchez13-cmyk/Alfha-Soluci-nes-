# ============================================================================
# ProGuard / R8 Rules for Android Security & Size Optimization
# ============================================================================

# Preserve line numbers and source file attributes for stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Preserve annotations, signatures, and inner classes for reflection & serialization
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep custom Application and Activity entry points
-keep public class * extends android.app.Application
-keep public class * extends android.app.Activity

# Keep ViewModel classes and their public constructors
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}

# ============================================================================
# Jetpack Compose & Material 3 Rules
# ============================================================================
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.ui.** { *; }

# ============================================================================
# Kotlin Coroutines
# ============================================================================
-keepclassmembers class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# ============================================================================
# Room Database
# ============================================================================
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keepclassmembers class * {
    @androidx.room.* *;
}

# ============================================================================
# Moshi & Retrofit / OkHttp Networking Rules
# ============================================================================
-keep class com.squareup.moshi.** { *; }
-keepclassmembers class * {
    @com.squareup.moshi.* *;
}
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# ============================================================================
# Firebase Rules
# ============================================================================
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# ============================================================================
# Application Data Models and Entities
# Keep all data models in com.example domain from obfuscation/removal
# ============================================================================
-keep class com.example.data.** { *; }
-keep class com.example.model.** { *; }
-keep class com.alfredo.medusaalfha.** { *; }
-keep class net.sqlcipher.** { *; }
-dontwarn net.sqlcipher.**
-keep class androidx.security.crypto.** { *; }
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}
