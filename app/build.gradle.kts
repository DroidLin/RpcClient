plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
//    id("kotlin-kapt")
}

android {
    namespace = "com.dst.rpc"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.dst.rpc"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    kotlin {
//        sourceSets.main {
//            kotlin.srcDir("build${File.separator}generated${File.separator}ksp${File.separator}main${File.separator}kotlin")
//        }
//        sourceSets.test {
//            kotlin.srcDir("build${File.separator}generated${File.separator}ksp${File.separator}test${File.separator}kotlin")
//        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.7.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.2")

    implementation("androidx.lifecycle:lifecycle-common:2.6.1")

    implementation(project(":core"))
    implementation(project(":core-annotation"))
    implementation(project(":android-extensions"))
//    implementation(project(":socket-extensions"))
//    kapt(project(":kapt-compiler"))
    ksp(project(":ksp-compiler"))
}