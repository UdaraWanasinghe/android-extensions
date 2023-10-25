@file:Suppress("UnstableApiUsage")

import com.aureusapps.gradle.PublishLibraryConstants.GROUP_ID

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.aureusapps.gradle.update-version")
}

class Props(project: Project) {
    val groupId = project.findProperty(GROUP_ID).toString()
    val artifactId = "extensions.example"
    val versionCode = 5
    val versionName = "1.0.4"
}

val props = Props(project)

android {
    namespace = "${props.groupId}.${props.artifactId}"
    compileSdk = 33
    defaultConfig {
        applicationId = "${props.groupId}.${props.artifactId}"
        minSdk = 21
        targetSdk = 33
        versionCode = props.versionCode
        versionName = props.versionName
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
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)

    testImplementation(libs.junit)

    androidTestImplementation(libs.junit.ext)
    androidTestImplementation(libs.espresso.core)
}