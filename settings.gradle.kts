// Root settings.gradle (or settings.kts equivalent)

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        // Ensure you add any required custom repositories here if needed later
    }

    plugins {
        id("com.android.application") version "8.13.0"

        // 🌟 FIX: Plugin versions aligned with app build.gradle.kts
        id("org.jetbrains.kotlin.android") version "1.9.10"

        // 🌟 FIX: Add the dependency for the Google services Gradle plugin
        id("com.google.gms.google-services") version "4.4.1"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Wardeobe2"
include(":app")