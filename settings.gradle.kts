pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "DstRpcClient"
include(":app")
include(":core")
include(":android-extensions")
include(":core-annotation")
include("socket-rpc-extensions")
include(":socket-extensions")
