-keep class androidx.media3.** { *; }
-keep class com.google.android.exoplayer2.** { *; }
-dontwarn androidx.media3.**

# Keep all app model/data classes — Firestore uses reflection to deserialize
-keep class com.legalstaan.app.LiveSession { *; }
-keepclassmembers class com.legalstaan.app.LiveSession {
    <init>();
    <fields>;
    <methods>;
}

# Keep all Firestore model POJOs (safe catch-all for custom data classes)
-keep class com.legalstaan.app.** { *; }

# Firebase Firestore
-keep class com.google.firebase.firestore.** { *; }
-keepnames class com.google.firebase.firestore.** { *; }
