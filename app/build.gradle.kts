// In your app/build.gradle.kts (Module: app)

import java.util.Properties

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // Applies the Google Services plugin defined in the project-level file
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.wardeobe"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.wardeobe"
        minSdk = 25
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Expose keys to your Kotlin code
        buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"${localProperties.getProperty("CLOUDINARY_CLOUD_NAME")}\"")
        buildConfigField("String", "CLOUDINARY_API_KEY", "\"${localProperties.getProperty("CLOUDINARY_API_KEY")}\"")
        buildConfigField("String", "CLOUDINARY_API_SECRET", "\"${localProperties.getProperty("CLOUDINARY_API_SECRET")}\"")
        buildConfigField("String", "FREEPIK_API_KEY", "\"${localProperties.getProperty("FREEPIK_API_KEY")}\"")
    }

    // ✅ STEP 4: Set NDK version for 16 KB page size alignment support
    ndkVersion = "26.1.10909125"

    // Fix for Apache dependency conflicts & ✅ STEP 5: Fix packaging for 16 KB support
    packaging {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/notice.txt",
                "META-INF/ASL2.0"
            )
        }
        jniLibs {
            // Ensures shared libraries are page-aligned and uncompressed in the APK
            useLegacyPackaging = false
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true // Ensure this is true to generate the BuildConfig class
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
}

dependencies {
    // ✅ STEP 2: Upgraded Fresco dependencies supporting 16 KB page size
    implementation("com.facebook.fresco:fresco:3.2.0")
    implementation("com.facebook.fresco:imagepipeline-okhttp3:3.2.0")
    implementation("com.facebook.fresco:animated-gif:3.2.0")

    // ---------------------------------------------------------------------
    // 🌐 Firebase & Google SDK Dependencies
    // ---------------------------------------------------------------------
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))

    // Authentication, Storage, and Database
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")

    // Coroutines (Kept standard version)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    // ---------------------------------------------------------------------
    // Desugaring dependency
    // ---------------------------------------------------------------------
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // ---------------------------------------------------------------------
    // ☁️ Cloudinary Dependencies (Existing)
    // ---------------------------------------------------------------------
    implementation("com.cloudinary:cloudinary-http44:1.37.0") {
        exclude(group = "com.cloudinary", module = "cloudinary-core")
    }
    implementation("com.cloudinary:cloudinary-android:2.4.0")

    // ---------------------------------------------------------------------
    // ✅ Core and Compose (Existing)
    // ---------------------------------------------------------------------
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.2")

    // Jetpack Compose
    implementation(platform("androidx.compose:compose-bom:2024.09.02"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material:material-icons-extended:1.7.0")
    implementation(libs.androidx.compose.material) // Make sure this matches your Version Catalog if using one
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.0")

    // Coil (Image Loading)
    implementation("io.coil-kt:coil-compose:2.6.0")

    // ---------------------------------------------------------------------
    // 🌐 Networking and Utilities
    // ---------------------------------------------------------------------
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.json:json:20240303")
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
    implementation("com.google.accompanist:accompanist-swiperefresh:0.32.0")

    // ---------------------------------------------------------------------
    // 🧪 Testing
    // ---------------------------------------------------------------------
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.09.02"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}