plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    id("kotlin-kapt")
    id("maven-publish")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation(project(":core-annotation"))

    implementation(project(":core"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.7.2")
    implementation("com.github.DroidLin:serializer:1.0.0")
    kapt(project(":kapt-compiler"))
}

publishing {
    publications {
        register<MavenPublication>("java") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            afterEvaluate {
                from(components["java"])
            }
        }
    }
    repositories {
        maven {
            name = "repositoryLocalRepo"
            url = uri("${rootProject.projectDir}/repo")
        }
    }
}