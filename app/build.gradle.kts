import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// Release signing key: from VENTY_* environment variables (GitHub Actions) or keystore.properties
// in the repo root (local, gitignored). Without either, release builds fall back to the debug key.
val releaseKey: Map<String, String>? = run {
    val env = System.getenv()
    if (!env["VENTY_KEYSTORE_FILE"].isNullOrBlank()) {
        return@run mapOf(
            "storeFile" to env.getValue("VENTY_KEYSTORE_FILE"),
            "storePassword" to env["VENTY_KEYSTORE_PASSWORD"].orEmpty(),
            "keyAlias" to env["VENTY_KEY_ALIAS"].orEmpty(),
            "keyPassword" to env["VENTY_KEY_PASSWORD"].orEmpty(),
        )
    }
    val file = rootProject.file("keystore.properties")
    if (!file.exists()) return@run null
    val props = Properties().apply { file.inputStream().use { load(it) } }
    props.stringPropertyNames().associateWith { props.getProperty(it) }
}

android {
    namespace = "io.github.orth4x0r.venty"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.orth4x0r.venty"
        minSdk = 26
        targetSdk = 36
        versionCode = 8
        versionName = "0.3.2"
    }

    signingConfigs {
        releaseKey?.let { key ->
            create("release") {
                storeFile = rootProject.file(key.getValue("storeFile"))
                storePassword = key.getValue("storePassword")
                keyAlias = key.getValue("keyAlias")
                keyPassword = key.getValue("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.process)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.kotlinx.coroutines.android)
    // Talks to the Wear OS tile in :wear.
    implementation(libs.play.services.wearable)
    implementation(libs.kotlinx.coroutines.play.services)
    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
