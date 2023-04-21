import com.bmuschko.gradle.docker.tasks.container.DockerCreateContainer
import com.bmuschko.gradle.docker.tasks.container.DockerStartContainer
import com.bmuschko.gradle.docker.tasks.container.DockerStopContainer
import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.Dockerfile
import java.net.ServerSocket

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.gradle.docker)
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(projects.data.models)
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
    implementation(files("lib/secp256k1-kmp-jni-jvm-darwin-0.7.1.jar"))

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

val createDockerfile by tasks.creating(Dockerfile::class) {
    from("scsibug/nostr-rs-relay:latest")
}

val buildImage by tasks.creating(DockerBuildImage::class) {
    dependsOn(createDockerfile)
    images.add("scsibug/nostr-rs-relay:latest")
}

val createContainer by tasks.creating(DockerCreateContainer::class) {
    dependsOn(buildImage)
    targetImageId(buildImage.imageId)
    hostConfig.portBindings.set(listOf("7707:8080"))
    hostConfig.autoRemove.set(true)
}

val startContainer by tasks.creating(DockerStartContainer::class) {
    onlyIf { _ ->
        try {
            val s = ServerSocket(7707)
            s.close()
            true
        } catch (_: java.io.IOException) {
            false
        }
    }
    dependsOn(createContainer)
    targetContainerId(createContainer.containerId)
}

val stopContainer by tasks.creating(DockerStopContainer::class) {
    onlyIf { startContainer.enabled }
    targetContainerId(createContainer.containerId)
}

tasks.withType<Test>().configureEach {
    dependsOn(startContainer)
}
