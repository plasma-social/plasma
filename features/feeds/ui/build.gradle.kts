plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.paparazzi)
    kotlin("kapt")
}

android {
    namespace = "social.plasma.features.feeds.ui"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles("proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(projects.features.discovery.screens)
    implementation(projects.features.feeds.screens)
    implementation(projects.common.ui)
    implementation(projects.data.models)

    implementation(libs.accompanist.placeholder)
    implementation(libs.androidx.paging.compose)

    implementation(libs.circuit.core)
    implementation(libs.circuit.overlay)
    implementation(libs.coil.compose)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.compose.material.materialicons)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.ui)
    implementation(libs.compose.ui.uitoolingpreview)
    implementation(libs.compose.ui.util)

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    implementation(libs.nostrino)
    implementation(libs.timber)

    debugImplementation(libs.compose.ui.test.manifest)
    debugImplementation(libs.compose.ui.uitooling)

    testImplementation(projects.common.utils.fakes)
    testImplementation(projects.common.ui.testutils)
    testImplementation(libs.junit)
    testImplementation(libs.testparameterinjector)
}
