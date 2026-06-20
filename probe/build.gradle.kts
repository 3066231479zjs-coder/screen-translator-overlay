plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.jason.screentranslator.probe"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.jason.screentranslator.probe"
        minSdk = 23
        targetSdk = 28
        versionCode = 1
        versionName = "0.0.1"
    }

    signingConfigs {
        create("release") {
            storeFile = file("/home/ubuntu/secure-keystores/screen-translator-overlay-release.jks")
            storePassword = providers.environmentVariable("SCREEN_TRANSLATOR_STORE_PASSWORD").orNull ?: loadStorePassword()
            keyAlias = "screen-translator-overlay"
            keyPassword = providers.environmentVariable("SCREEN_TRANSLATOR_STORE_PASSWORD").orNull ?: loadStorePassword()
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            isDebuggable = false
        }
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

fun loadStorePassword(): String {
    val envFile = file("/home/ubuntu/secure-keystores/screen-translator-overlay-release.env")
    return envFile.readLines()
        .first { it.startsWith("SCREEN_TRANSLATOR_STORE_PASSWORD=") }
        .substringAfter("=")
}
