plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.molecule)
    kotlin("kapt")
}

android {
    namespace = "social.plasma.features.profile.presenters"
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
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
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

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(projects.domain)
    implementation(projects.features.profile.screens)
    implementation(projects.features.feeds.presenters)
    implementation(projects.features.feeds.screens)
    implementation(projects.repositories.api)
    implementation(projects.common.screens)
    implementation(projects.common.utils.api)
    implementation(projects.domain)
    implementation(projects.data.models)

    implementation(libs.androidx.paging.common)
    implementation(libs.circuit.core)
    implementation(libs.circuit.retained)
    implementation(libs.nostrino)
    implementation(libs.timber)

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.moshi.core)

    testImplementation(projects.repositories.fakes)
    testImplementation(projects.common.utils.fakes)
    testImplementation(libs.circuit.test)
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.testparameterinjector)
    testImplementation(libs.truth)

    coreLibraryDesugaring(libs.android.desugaring)
}
