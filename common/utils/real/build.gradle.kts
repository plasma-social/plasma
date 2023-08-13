plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    kotlin("kapt")
}

android {
    namespace = "social.plasma.shared.utils.real"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
}

dependencies {
    api(projects.common.utils.api)
    implementation(projects.data.models)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui.ui)

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    implementation(libs.icu4j)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    coreLibraryDesugaring(libs.android.desugaring)
}
