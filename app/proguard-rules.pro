# Keep Gson-related classes
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**

# Keep generic types for Gson
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# Keep all data classes used in export/import (critical!)
-keep class com.example.securevault.crypto.ExportImportManager$** { *; }
-keep class com.example.securevault.crypto.ExportImportManager$ExportData { *; }
-keep class com.example.securevault.crypto.ExportImportManager$AllData { *; }
-keep class com.example.securevault.crypto.ExportImportManager$PlainCredential { *; }
-keep class com.example.securevault.crypto.ExportImportManager$PlainBill { *; }
-keep class com.example.securevault.crypto.ExportImportManager$PlainNote { *; }

# Keep Room entities and DAOs
-keep class com.example.securevault.data.** { *; }

# Keep all model classes used with Gson
-keep class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Prevent obfuscation of enum values
-keepclassmembers enum * { *; }

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Optimize but don't remove or rename
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
