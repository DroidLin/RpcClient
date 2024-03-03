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
include(":core-rpc")
include(":android-rpc-extensions")
include(":core-rpc-annotation")
include("socket-rpc-extensions")
include(":socket-rpc-extensions")
