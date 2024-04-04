-keep class com.dst.rpc.INoProguard {*;}
-keepclasseswithmembernames class * implements com.dst.rpc.INoProguard {*;}

-keep class kotlin.Function {*;}
-keepclasseswithmembernames class * implements kotlin.Function {*;}

-keepclasseswithmembernames class * implements android.os.Parcelable {*;}

-keepattributes *Annotation*
-keep class kotlin.** { *; }
-keep class org.jetbrains.** { *; }