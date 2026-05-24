# Keep line numbers and source file names for readable crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Kotlin metadata (required for coroutines and DataStore reflection)
-keepattributes *Annotation*
-keep class kotlin.Metadata { *; }

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# WorkManager — workers are instantiated by class name
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# AccessibilityService — instantiated by the system
-keep class * extends android.accessibilityservice.AccessibilityService

# Firebase Crashlytics
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keep class com.google.firebase.crashlytics.** { *; }

# DataStore — serialization via Kotlin property delegation
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}
