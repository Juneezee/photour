# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html
-keep public class * extends androidx.fragment.app.Fragment{}
-keep public class com.google.android.libraries.maps.** { *; }

-overloadaggressively
-repackageclasses
