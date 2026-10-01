plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
}

android {
    namespace = "com.warrior.tracker"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.warrior.tracker"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        debug {
            // Debug APK produced by CI is unsigned-friendly (default debug keystore).
        }
        release {
            // Signing is configured in Phase 6 via GitHub Secrets; until then release
            // builds stay unsigned on purpose (Local-first app, no store upload yet).
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    lint {
        // Non-blocking for now on purpose: the report is published as a CI artifact so findings are
        // visible, but a newly-added check must not be able to turn the phase gate (sec.16: green
        // build + installable APK) red on an unrelated rule. Phase 6 flips abortOnError to true
        // once a lint baseline has been committed.
        abortOnError = false
        warningsAsErrors = false
        checkReleaseBuilds = false
        xmlReport = true
        htmlReport = true
        // Correctness issues that matter for a local-first, offline, RTL app.
        error += setOf("MissingTranslation", "ExtraTranslation", "FullBackupContent", "NewApi", "RtlHardcoded")
        disable += setOf("GradleDependency", "AndroidGradlePluginVersion")
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// Room schema export — required by ARCHITECTURE.md sec.12 ("نگهداری Schemaها در Git") and by
// the Phase-5 MigrationTestHelper. The path MUST resolve inside the repository: the Phase-0 value
// "$projectDir/../../docs/schemas" resolved to <repo>/../docs/schemas, i.e. one level ABOVE the
// project root, so the JSON was written outside the working tree and never committed.
ksp {
    arg("room.schemaLocation", "$rootDir/docs/schemas")
    arg("room.incremental", "true")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    androidTestImplementation(libs.androidx.room.testing)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
