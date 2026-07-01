# R8/ProGuard configuration for iDorm release build

# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**

# Kotlinx Serialization
-keepattributes *Annotation*,Signature
-dontwarn kotlinx.serialization.**
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
    @kotlinx.serialization.Serializable <fields>;
}

# Keep Hilt/Dagger classes
-keep class class.to.keep { *; }
-dontwarn dagger.hilt.internal.**

# Keep Firebase models and Firestore
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-dontwarn com.google.firebase.**
-keep class com.google.firebase.** { *; }

# Keep App Network Models
-keep class com.bdsoftware.idorm.core.network.model.** { *; }
-keep class com.bdsoftware.idorm.core.model.** { *; }

# Firebase Crashlytics
-keepattributes SourceFile,LineNumberTable

