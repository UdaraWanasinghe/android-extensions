plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    `maven-publish`
    kotlin("kapt")
    signing
}

project.extra["PUBLISH_GROUP_ID"] = "com.aureusapps.android"
project.extra["PUBLISH_VERSION"] = rootProject.extra["VERSION_NAME"]
project.extra["PUBLISH_ARTIFACT_ID"] = "extensions"

android {
    namespace = "com.aureusapps.android.extensions"
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

apply(from = "$rootDir/scripts/publish-module.gradle.kts")