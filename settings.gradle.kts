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

        // 🌟 FIX: Plugin versions aligned with libs.versions.toml
        id("org.jetbrains.kotlin.android") version "2.0.21"
        id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
        id("org.jetbrains.kotlin.kapt") version "2.0.21"

        // 🌟 FIX: Add the dependency for the Google services and Hilt Gradle plugins
        id("com.google.gms.google-services") version "4.4.1"
        id("com.google.dagger.hilt.android") version "2.51"
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