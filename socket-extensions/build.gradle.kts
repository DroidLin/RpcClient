plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    implementation(project(":core"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.7.2")
}