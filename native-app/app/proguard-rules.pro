# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile


-keep class com.shockwave.**

-keepattributes Signature
-keep class com.firebase.** { *; }
-keep class org.apache.** { *; }
-keepnames class com.fasterxml.jackson.** { *; }
-keepnames class javax.servlet.** { *; }
-keepnames class org.ietf.jgss.** { *; }
-dontwarn org.w3c.dom.**
-dontwarn org.joda.time.**
-dontwarn org.shaded.apache.**
-dontwarn org.ietf.jgss.**

#-keep class com.google.gson.stream.** { *; }
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    public *;
}

# Keep Glide custom transformations (updateDiskCacheKey is called reflectively)
-keep class arivihan.technologies.doubtbuzzter2.helper.** extends com.bumptech.glide.load.resource.bitmap.BitmapTransformation { *; }
-keep class arivihan.technologies.doubtbuzzter2.helper.** implements com.bumptech.glide.module.GlideModule { *; }

-keepattributes *Annotation*

-keepclassmembers class arivihan.technologies.doubtbuzzter2.model.** {
*;
}

-keepclassmembers class arivihan.technologies.doubtbuzzter2.constants.** {
*;
}

-keep class com.google.firebase.example.fireeats.java.model.** { *; }
-keep class com.google.firebase.example.fireeats.kotlin.model.** { *; }

# Keep model classes and all members
-keep class arivihan.technologies.doubtbuzzter2.model.** {
    <fields>;
    <methods>;
}

# Razorpay
-keep class com.razorpay.** { *; }
-keep interface com.razorpay.** { *; }

# Branch.io — no rules existed for this SDK; under R8 (minifyEnabled=true) its
# reflection-based event/session classes can get stripped or renamed, and every
# BranchEvent/logEvent call in this app is wrapped in try/catch, so a broken
# release build fails completely silently instead of crashing.
-keep class io.branch.** { *; }
-keep interface io.branch.** { *; }
-dontwarn io.branch.**
-keep class com.google.android.gms.ads.identifier.** { *; }
-dontwarn com.google.android.gms.ads.identifier.**

-dontwarn okio.**
-dontwarn retrofit2.Call
-dontnote retrofit2.Platform$IOS$MainThreadExecutor
-keep class android.support.v7.widget.RecyclerView { *; }

# ── Retrofit / OkHttp ────────────────────────────────────────────────
# Retrofit does reflection on generic parameters and annotations
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

# Keep service interfaces and their annotations
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>
-keep,allowobfuscation,allowshrinking @retrofit2.http.* class *
-keep interface arivihan.technologies.doubtbuzzter2.network.** { *; }

-dontwarn retrofit2.**
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit

# OkHttp
-dontwarn okhttp3.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-keepclassmembers class okhttp3.internal.publicsuffix.PublicSuffixDatabase { *; }

-keep class com.google.firebase.example.fireeats.model.** { *; }
-keep class com.google.android.gms.ads.** { *; }
-dontwarn okio.**
-keep class androidx.appcompat.widget.** { *; }
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken
-keep class ** extends com.google.gson.reflect.TypeToken
-keep public class * implements java.lang.reflect.Type

-dontwarn com.google.android.gms.auth.api.credentials.Credential$Builder
-dontwarn com.google.android.gms.auth.api.credentials.Credential
-dontwarn com.google.android.gms.auth.api.credentials.CredentialRequest$Builder
-dontwarn com.google.android.gms.auth.api.credentials.CredentialRequest
-dontwarn com.google.android.gms.auth.api.credentials.CredentialRequestResponse
-dontwarn com.google.android.gms.auth.api.credentials.Credentials
-dontwarn com.google.android.gms.auth.api.credentials.CredentialsClient
-dontwarn com.google.android.gms.auth.api.credentials.CredentialsOptions$Builder
-dontwarn com.google.android.gms.auth.api.credentials.CredentialsOptions
-dontwarn com.google.android.gms.auth.api.credentials.HintRequest$Builder
-dontwarn com.google.android.gms.auth.api.credentials.HintRequest
-dontwarn lombok.bytecode.PoolConstantsApp
-dontwarn lombok.bytecode.PostCompilerApp
-dontwarn lombok.bytecode.PreventNullAnalysisRemover
-dontwarn lombok.bytecode.SneakyThrowsRemover
-dontwarn lombok.core.Main$LicenseApp
-dontwarn lombok.core.Main$VersionApp
-dontwarn lombok.core.PublicApiCreatorApp
-dontwarn lombok.core.configuration.ConfigurationApp
-dontwarn lombok.core.handlers.SneakyThrowsAndCleanupDependencyInfo
-dontwarn lombok.core.runtimeDependencies.CreateLombokRuntimeApp
-dontwarn lombok.delombok.DelombokApp
-dontwarn lombok.eclipse.agent.MavenEcjBootstrapApp
-dontwarn lombok.eclipse.handlers.HandleAccessors
-dontwarn lombok.eclipse.handlers.HandleBuilder
-dontwarn lombok.eclipse.handlers.HandleBuilderDefault
-dontwarn lombok.eclipse.handlers.HandleCleanup
-dontwarn lombok.eclipse.handlers.HandleConstructor$HandleAllArgsConstructor
-dontwarn lombok.eclipse.handlers.HandleConstructor$HandleNoArgsConstructor
-dontwarn lombok.eclipse.handlers.HandleConstructor$HandleRequiredArgsConstructor
-dontwarn lombok.eclipse.handlers.HandleData
-dontwarn lombok.eclipse.handlers.HandleDelegate
-dontwarn lombok.eclipse.handlers.HandleEqualsAndHashCode
-dontwarn lombok.eclipse.handlers.HandleExtensionMethod
-dontwarn lombok.eclipse.handlers.HandleFieldDefaults
-dontwarn lombok.eclipse.handlers.HandleFieldNameConstants
-dontwarn lombok.eclipse.handlers.HandleGetter
-dontwarn lombok.eclipse.handlers.HandleHelper
-dontwarn lombok.eclipse.handlers.HandleJacksonized
-dontwarn lombok.eclipse.handlers.HandleLog$HandleCommonsLog
-dontwarn lombok.eclipse.handlers.HandleLog$HandleCustomLog
-dontwarn lombok.eclipse.handlers.HandleLog$HandleFloggerLog
-dontwarn lombok.eclipse.handlers.HandleLog$HandleJBossLog
-dontwarn lombok.eclipse.handlers.HandleLog$HandleJulLog
-dontwarn lombok.eclipse.handlers.HandleLog$HandleLog4j2Log
-dontwarn lombok.eclipse.handlers.HandleLog$HandleLog4jLog
-dontwarn lombok.eclipse.handlers.HandleLog$HandleSlf4jLog
-dontwarn lombok.eclipse.handlers.HandleLog$HandleXSlf4jLog
-dontwarn lombok.eclipse.handlers.HandleNonNull
-dontwarn lombok.eclipse.handlers.HandlePrintAST
-dontwarn lombok.eclipse.handlers.HandleSetter
-dontwarn lombok.eclipse.handlers.HandleSneakyThrows
-dontwarn lombok.eclipse.handlers.HandleStandardException
-dontwarn lombok.eclipse.handlers.HandleSuperBuilder
-dontwarn lombok.eclipse.handlers.HandleSynchronized
-dontwarn lombok.eclipse.handlers.HandleToString
-dontwarn lombok.eclipse.handlers.HandleUtilityClass
-dontwarn lombok.eclipse.handlers.HandleVal
-dontwarn lombok.eclipse.handlers.HandleValue
-dontwarn lombok.eclipse.handlers.HandleWith
-dontwarn lombok.eclipse.handlers.HandleWithBy
-dontwarn lombok.eclipse.handlers.singulars.EclipseGuavaMapSingularizer
-dontwarn lombok.eclipse.handlers.singulars.EclipseGuavaSetListSingularizer
-dontwarn lombok.eclipse.handlers.singulars.EclipseGuavaTableSingularizer
-dontwarn lombok.eclipse.handlers.singulars.EclipseJavaUtilListSingularizer
-dontwarn lombok.eclipse.handlers.singulars.EclipseJavaUtilMapSingularizer
-dontwarn lombok.eclipse.handlers.singulars.EclipseJavaUtilSetSingularizer
-dontwarn lombok.installer.Installer$CommandLineInstallerApp
-dontwarn lombok.installer.Installer$CommandLineUninstallerApp
-dontwarn lombok.installer.Installer$GraphicalInstallerApp
-dontwarn lombok.installer.eclipse.AngularIDELocationProvider
-dontwarn lombok.installer.eclipse.EclipseLocationProvider
-dontwarn lombok.installer.eclipse.JbdsLocationProvider
-dontwarn lombok.installer.eclipse.MyEclipseLocationProvider
-dontwarn lombok.installer.eclipse.RhcrLocationProvider
-dontwarn lombok.installer.eclipse.RhdsLocationProvider
-dontwarn lombok.installer.eclipse.STS4LocationProvider
-dontwarn lombok.installer.eclipse.STSLocationProvider
-dontwarn lombok.javac.handlers.HandleAccessors
-dontwarn lombok.javac.handlers.HandleBuilder
-dontwarn lombok.javac.handlers.HandleBuilderDefault
-dontwarn lombok.javac.handlers.HandleBuilderDefaultRemove
-dontwarn lombok.javac.handlers.HandleBuilderRemove
-dontwarn lombok.javac.handlers.HandleCleanup
-dontwarn lombok.javac.handlers.HandleConstructor$HandleAllArgsConstructor
-dontwarn lombok.javac.handlers.HandleConstructor$HandleNoArgsConstructor
-dontwarn lombok.javac.handlers.HandleConstructor$HandleRequiredArgsConstructor
-dontwarn lombok.javac.handlers.HandleData
-dontwarn lombok.javac.handlers.HandleDelegate
-dontwarn lombok.javac.handlers.HandleEqualsAndHashCode
-dontwarn lombok.javac.handlers.HandleExtensionMethod
-dontwarn lombok.javac.handlers.HandleFieldDefaults
-dontwarn lombok.javac.handlers.HandleFieldNameConstants
-dontwarn lombok.javac.handlers.HandleGetter
-dontwarn lombok.javac.handlers.HandleHelper
-dontwarn lombok.javac.handlers.HandleJacksonized
-dontwarn lombok.javac.handlers.HandleLog$HandleCommonsLog
-dontwarn lombok.javac.handlers.HandleLog$HandleCustomLog
-dontwarn lombok.javac.handlers.HandleLog$HandleFloggerLog
-dontwarn lombok.javac.handlers.HandleLog$HandleJBossLog
-dontwarn lombok.javac.handlers.HandleLog$HandleJulLog
-dontwarn lombok.javac.handlers.HandleLog$HandleLog4j2Log
-dontwarn lombok.javac.handlers.HandleLog$HandleLog4jLog
-dontwarn lombok.javac.handlers.HandleLog$HandleSlf4jLog
-dontwarn lombok.javac.handlers.HandleLog$HandleXSlf4jLog
-dontwarn lombok.javac.handlers.HandleNonNull
-dontwarn lombok.javac.handlers.HandlePrintAST
-dontwarn lombok.javac.handlers.HandleSetter
-dontwarn lombok.javac.handlers.HandleSneakyThrows
-dontwarn lombok.javac.handlers.HandleStandardException
-dontwarn lombok.javac.handlers.HandleSuperBuilder
-dontwarn lombok.javac.handlers.HandleSuperBuilderRemove
-dontwarn lombok.javac.handlers.HandleSynchronized
-dontwarn lombok.javac.handlers.HandleToString
-dontwarn lombok.javac.handlers.HandleUtilityClass
-dontwarn lombok.javac.handlers.HandleVal
-dontwarn lombok.javac.handlers.HandleValue
-dontwarn lombok.javac.handlers.HandleWith
-dontwarn lombok.javac.handlers.HandleWithBy
-dontwarn lombok.javac.handlers.singulars.JavacGuavaMapSingularizer
-dontwarn lombok.javac.handlers.singulars.JavacGuavaSetListSingularizer
-dontwarn lombok.javac.handlers.singulars.JavacGuavaTableSingularizer
-dontwarn lombok.javac.handlers.singulars.JavacJavaUtilListSingularizer
-dontwarn lombok.javac.handlers.singulars.JavacJavaUtilMapSingularizer
-dontwarn lombok.javac.handlers.singulars.JavacJavaUtilSetSingularizer
-dontwarn lombok.eclipse.handlers.HandleLocked
-dontwarn lombok.eclipse.handlers.HandleLockedRead
-dontwarn lombok.eclipse.handlers.HandleLockedWrite
-dontwarn lombok.javac.handlers.HandleLocked
-dontwarn lombok.javac.handlers.HandleLockedRead
-dontwarn lombok.javac.handlers.HandleLockedWrite
-dontwarn lombok.javac.handlers.HandleSingularRemove
-dontwarn proguard.annotation.Keep
-dontwarn proguard.annotation.KeepClassMembers
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken
-keep public class * implements java.lang.reflect.Type
-dontwarn com.google.android.apps.nbu.paisa.inapp.client.api.PaymentsClient
-dontwarn com.google.android.apps.nbu.paisa.inapp.client.api.Wallet
-dontwarn com.google.android.apps.nbu.paisa.inapp.client.api.WalletUtils

# Keep all subclasses of TypeReference
-keep class * extends com.fasterxml.jackson.core.type.TypeReference
# Keep Gson classes
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

# Keep generic type info
-keepattributes Signature
# Keep Jackson classes
-keep class com.fasterxml.jackson.** { *; }
-dontwarn com.fasterxml.jackson.databind.**
-dontwarn com.fasterxml.jackson.annotation.**


# Keep Gson TypeToken
-keep class com.google.gson.reflect.TypeToken { *; }

# Keep Gson classes

# Keep generic type info
-keepattributes Signature
-keepattributes *Annotation*

# Keep WebView file/image payload DTOs so Gson field names survive ProGuard
-keep class arivihan.technologies.doubtbuzzter2.activity.web_view.WebViewByUrlActivity$ImagePayload { *; }
-keep class arivihan.technologies.doubtbuzzter2.activity.web_view.WebViewByUrlActivity$FilePayload { *; }

# Strip verbose/debug/info logs in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
