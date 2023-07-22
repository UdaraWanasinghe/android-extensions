@file:Suppress("UnstableApiUsage")

import com.aureusapps.gradle.PublishLibraryConstants.GROUP_ID

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.aureusapps.gradle.update-version")
    id("com.aureusapps.gradle.publish-library")
    kotlin("kapt")
}

val groupIdProperty = findProperty(GROUP_ID)?.toString()

android {
    namespace = "$groupIdProperty.extensions"
    compileSdk = 33
    defaultConfig {
        minSdk = 21
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

publishLibrary {
    groupId = groupIdProperty
    artifactId = "extensions"
    versionName = "1.0.2"
    libName = "Extensions"
    libDescription = "Useful extension functions and utility classes for android development."
    libUrl = "https://github.com/UdaraWanasinghe/android-extensions"
    licenseName = "MIT License"
    licenseUrl = "https://github.com/UdaraWanasinghe/android-extensions/blob/main/LICENSE"
    devId = "UdaraWanasinghe"
    devName = "Udara Wanasinghe"
    devEmail = "udara.developer@gmail.com"
    scmConnection = "scm:git:github.com/UdaraWanasinghe/android-extensions.git"
    scmDevConnection = "scm:git:ssh://github.com/UdaraWanasinghe/android-extensions.git"
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.room.runtime)
    kapt(libs.room.compiler)
    implementation(libs.okhttp)
    implementation(libs.activity.ktx)
    implementation(libs.documentfile)
    implementation(libs.espresso.core)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(libs.junit.ext)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.room.runtime)
    kaptAndroidTest(libs.room.compiler)
    androidTestImplementation(libs.okhttp)
    androidTestImplementation(libs.okhttp.mockwebserver)
}