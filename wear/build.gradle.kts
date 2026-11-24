import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

// The watch half: a Wear OS tile that shows the heater and toggles it through the phone. It
// shares the phone app's applicationId and signing key, which the Data Layer requires.

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
    namespace = "io.github.orth4x0r.venty.wear"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.orth4x0r.venty"
        minSdk = 30
        targetSdk = 34
        versionCode = 6
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
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.wear.tiles)
    implementation(libs.androidx.wear.protolayout)
    implementation(libs.androidx.wear.protolayout.material)
    implementation(libs.guava)
    implementation(libs.play.services.wearable)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.kotlinx.coroutines.guava)
}
