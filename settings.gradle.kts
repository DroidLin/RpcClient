pluginManagement {
    repositories {
        maven {
            url = uri("${rootProject.projectDir}/repo")
        }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven {
            url = uri("${rootProject.projectDir}/repo")
        }
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "DstRpcClient"
include(":app")
include(":core")
include(":android-extensions")
include(":core-annotation")
include(":socket-extensions")
include(":ksp-compiler")
include(":kapt-compiler")
