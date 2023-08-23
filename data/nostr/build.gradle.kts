import java.net.ServerSocket

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    kotlin("kapt")
}

android {
    namespace = "social.plasma.nostr"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(projects.data.models)
    implementation(projects.data.daos)
    implementation(libs.androidx.core.ktx)
    implementation(libs.coroutines.android)

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    implementation(libs.moshi.core)
    implementation(libs.moshi.kotlin)

    implementation(libs.nostrino)

    implementation(libs.okhttp.interceptor.logging)
    implementation(libs.rxjava)

    implementation(libs.scarlet.core)
    implementation(libs.scarlet.websocket.okhttp)
    implementation(libs.scarlet.messageadapter.moshi)
    implementation(libs.scarlet.streamadapter.coroutines)
    implementation(libs.scarlet.streamadapter.rxjava)

    implementation(libs.secp256k1.jvm)
    implementation(libs.secp256k1.jvm.jni)

    implementation(libs.timber)

    testImplementation(libs.junit)
    testImplementation(libs.kotest.assertions)
    testImplementation(libs.kotest.property)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.nostrino)
    testImplementation(libs.turbine)

    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)

    coreLibraryDesugaring(libs.android.desugaring)
}

android.testOptions {
    unitTests.all {
        it.testLogging {
            showStandardStreams = true

        }
        it.useJUnitPlatform()
    }
}
