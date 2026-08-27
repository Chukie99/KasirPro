# ==============================================================================
# ProGuard / R8 rules for KasirPro
# Keep model classes referenced via reflection by Room, Gson, Hilt
# ==============================================================================

# ── Keep Hilt / Dagger generated classes ──────────────────────────────────────
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class **$$Hilt_* { *; }

# ── AndroidX / AppCompat ──────────────────────────────────────────────────────
-keep class androidx.appcompat.** { *; }
-keep class androidx.room.** { *; }
-keep class androidx.room.paging.** { *; }

# ── Kotlinx Coroutines ────────────────────────────────────────────────────────
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.debug.internal.**

# ── Gson ──────────────────────────────────────────────────────────────────────
-keep class com.google.gson.** { *; }
-keep class com.kasirpro.data.model.** { *; }
-keepattributes Signature
-keepattributes *Annotation*, *AnnotationDefinition*

# ── Room (Entity / DAO / Database generated code) ─────────────────────────────
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Database class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.RoomDatabase

# ── Coil (image loader) ───────────────────────────────────────────────────────
-keep class coil.** { *; }
-keep class coil.compose.** { *; }

# ── escpos printer �iques ──────────────────────────────────────────────────────
-keep class com.github.iammert.** { *; }

# ── MPAndroidChart ────────────────────────────────────────────────────────────
-keep class com.github.mikephil.charting.** { *; }
