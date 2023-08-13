plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.paparazzi)
//    kotlin("kapt")
}

android {
    namespace = "social.plasma.ui.testutils"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles("proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
//    buildFeatures {
//        compose = true
//    }
//    composeOptions {
//        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
//    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("app.cash.paparazzi:paparazzi:1.2.0")
//    implementation(platform(libs.compose.bom))
//    implementation(libs.compose.material3)
//    implementation(libs.compose.material.materialicons)
//    implementation(libs.compose.ui.graphics)
//    implementation(libs.compose.ui.ui)
//    implementation(libs.compose.ui.uitoolingpreview)
//    implementation(libs.compose.ui.util)

//    debugImplementation(libs.compose.ui.test.manifest)
//    debugImplementation(libs.compose.ui.uitooling)

//    testImplementation(libs.junit)
//    testImplementation(libs.testparameterinjector)
}
