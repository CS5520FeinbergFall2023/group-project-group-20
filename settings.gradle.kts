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

rootProject.name = "groupprojectgroup20"
include(":app")
include(":unityLibrary")
project(":unityLibrary").projectDir =  File("..\\UnityProject\\androidBuild\\unityLibrary")
