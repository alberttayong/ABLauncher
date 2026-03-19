# Add project specific ProGuard rules here.
-keep class com.ablauncher.** { *; }
-keepclassmembers class * {
    @dagger.hilt.* *;
}
