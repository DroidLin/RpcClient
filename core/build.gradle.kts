plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    id("maven-publish")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
    withJavadocJar()
}

dependencies {
//    implementation(project(":core-annotation"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.7.2")
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