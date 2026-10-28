import java.io.FileInputStream
import java.util.Properties
import kotlin.apply

// Add the KSP plugin alias to your plugins block
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp) // Add this line
    alias(libs.plugins.hiltandroid)
}

val releaseRequested = gradle.startParameter.taskNames.any {
    it.contains("release", ignoreCase = true) || it.contains("bundle", ignoreCase = true)
}
val keystorePropsFile = listOf(
    rootProject.file("keystore.properties"),
    project.file("keystore.properties")
).firstOrNull { it.exists() }

val keystoreProps = Properties().apply {
    if (keystorePropsFile != null) {
        FileInputStream(keystorePropsFile).use { load(it) }
    }
}

fun prop(key: String): String? {
    val value = keystoreProps.getProperty(key)
        ?: keystoreProps.getProperty("\uFEFF$key")
        ?: when (key) {
            "storeFile" -> System.getenv("STORE_FILE")
            "storePassword" -> System.getenv("STORE_PASSWORD")
            "keyAlias" -> System.getenv("KEY_ALIAS")
            "keyPassword" -> System.getenv("KEY_PASSWORD")
            else -> null
        }
    return value?.trim()?.takeIf { it.isNotEmpty() }
}

val hasSigning = listOf("storeFile", "storePassword", "r330KeyAlias", "keyPassword").all { !prop(it).isNullOrBlank() }

if (releaseRequested && !hasSigning) {
    throw GradleException("Missing signing values: storeFile, storePassword, r330KeyAlias, keyPassword")
}

fun resolveStoreFile(path: String): File {
    val candidate = File(path)
    if (candidate.isAbsolute) return candidate
    val base = keystorePropsFile?.parentFile ?: rootProject.projectDir
    return File(base, path)
}

android {
    namespace = "com.rpay.v2"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.malahi.pointofsale"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }


    signingConfigs {
        create("r330Release") {
            if (hasSigning) {
                storeFile = resolveStoreFile(prop("storeFile")!!)
                storePassword = prop("storePassword")
                keyAlias = prop("r330KeyAlias")
                keyPassword = prop("keyPassword")
            }
        }
    }

    @Suppress("DEPRECATION")
    applicationVariants.all {
        val variant = this // Reference to the current variant
        outputs.all {
            val appName = "MalahiR330"
            val vName = variant.versionName ?: "0.0"
            val vCode = variant.versionCode
            val bType = variant.buildType.name
            val flavor = variant.flavorName

            val flavorPart = if (flavor.isNotEmpty()) "-${flavor}" else ""
            val newName = "${appName}${flavorPart}-v${vName}(${vCode})-${bType}.apk"

            (this as? com.android.build.gradle.internal.api.BaseVariantOutputImpl)?.outputFileName = newName
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".development"
            buildConfigField(
                "Boolean",
                "IS_DEV_ENV",
                "true"
            )
            buildConfigField(
                "String",
                "API_KEY",
                "\"Yt6XFOVW4wqmsGIm6CvTnS3/BnhgeSXEVgvLVVWwt4g=\""
            )
            buildConfigField(
                "String",
                "BASE_URL",
                "\"https://test.malahiremote.com/MalahiPosApi/api/\""
            )
            buildConfigField(
                "int",
                "TCP_PORT",
                "6060"
            )
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        release {
            applicationIdSuffix = ".app"
            buildConfigField(
                "Boolean",
                "IS_DEV_ENV",
                "false"
            )
            buildConfigField(
                "String",
                "API_KEY",
                "\"d0043da8-3e92-4b24-88a6-f50f549c5d6a-e6850be7-4743-4f82-9952-0a95ea5b457d\""
            )
            buildConfigField(
                "String",
                "BASE_URL",
                "\"https://remoteposapi.malahiremote.com/api/\""
            )
            buildConfigField(
                "int",
                "TCP_PORT",
                "5050"
            )
            isMinifyEnabled = false
            isShrinkResources = false
            if (hasSigning) signingConfig = signingConfigs.getByName("r330Release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.android.broadcast)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Add these dependencies for KSP Jetpack Composer
    implementation(libs.compose.destinations.core)
    ksp(libs.compose.destinations.ksp)

    // Room dependencies - ADD THESE NEW LINES
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Add these missing dependencies from your other Gradle file
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.sqlite.framework)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.coroutines.android)
    implementation(libs.coroutines.core)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter)
    implementation(libs.interceptor)

    // DataStore dependencies
    implementation(libs.androidx.datastore.core)
    implementation(libs.androidx.datastore.preferences)

    // NearPay SDK
    implementation(libs.nearpay.sdk)
    implementation(libs.location)

    implementation(libs.android.camera.core)
    implementation(libs.android.camera.camera2)
    implementation(libs.android.camera.lifecycle)
    implementation(libs.android.camera.view)
    implementation(libs.barcode.scanning)
    implementation(libs.pl.gif)
    implementation(libs.coil.image)
    implementation(libs.coil.gif)

    implementation(libs.zxing.sdk)

    // Hilt Navigation Compose
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.androidx.lifecycle.compose)

}