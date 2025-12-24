# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep all classes in our package
-keep class com.musictranscription.app.** { *; }

# Keep MIDI library classes
-keep class com.leff.midi.** { *; }

# Keep iText PDF classes
-keep class com.itextpdf.** { *; }
