pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    versionCatalogs {
        create("libs") {
            from("com.aureusapps.android:version-catalog:1.0.0")
        }
    }
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
}

rootProject.name = "android-extensions"
include("extensions", "example")