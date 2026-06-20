import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val releaseSigningEnv = file("/home/ubuntu/secure-keystores/screen-translator-overlay-release.env")
val releaseSigningProperties = Properties().apply {
    if (releaseSigningEnv.exists()) {
        releaseSigningEnv.inputStream().use { load(it) }
    }
}

android {
    namespace = "com.jason.screentranslator"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.jason.screentranslator"
        minSdk = 26
        targetSdk = 33
        versionCode = 3
        versionName = "0.1.2"
        buildConfigField("String", "TRANSLATION_BASE_URL", "\"${providers.gradleProperty("translationBaseUrl").orNull ?: ""}\"")
        buildConfigField("String", "TRANSLATION_API_KEY", "\"${providers.gradleProperty("translationApiKey").orNull ?: ""}\"")
        buildConfigField("String", "TRANSLATION_MODEL", "\"${providers.gradleProperty("translationModel").orNull ?: ""}\"")
    }

    signingConfigs {
        create("release") {
            storeFile = file("/home/ubuntu/secure-keystores/screen-translator-overlay-release.jks")
            storePassword = releaseSigningProperties.getProperty("SCREEN_TRANSLATOR_STORE_PASSWORD", "")
            keyAlias = "screen-translator-overlay"
            keyPassword = releaseSigningProperties.getProperty("SCREEN_TRANSLATOR_STORE_PASSWORD", "")
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = signingConfigs.getByName("release")
            isDebuggable = false
        }
    }

    buildFeatures {
        buildConfig = true
    }

    lint {
        checkReleaseBuilds = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
}
