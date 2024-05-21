@file:Suppress("UnstableApiUsage")

import com.aureusapps.gradle.PublishLibraryConstants.GROUP_ID
import com.aureusapps.gradle.PublishLibraryConstants.VERSION_NAME

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.aureusapps.gradle.update-version")
    id("com.aureusapps.gradle.publish-library")
    kotlin("kapt")
}

class Props(project: Project) {
    val groupId = project.findProperty(GROUP_ID) as String
    val versionName = project.findProperty(VERSION_NAME) as String
}

val props = Props(project)

android {
    namespace = "${props.groupId}.extensions"
    compileSdk = 34
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
    buildFeatures {
        buildConfig = true
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

publishLibrary {
    groupId.set(props.groupId)
    artifactId.set("extensions")
    versionName.set(props.versionName)
    libName.set("Extensions")
    libDescription.set("Useful extension functions and utility classes for android development.")
    libUrl.set("https://github.com/UdaraWanasinghe/android-extensions")
    licenseName.set("MIT License")
    licenseUrl.set("https://github.com/UdaraWanasinghe/android-extensions/blob/main/LICENSE")
    devId.set("UdaraWanasinghe")
    devName.set("Udara Wanasinghe")
    devEmail.set("udara.developer@gmail.com")
    scmConnection.set("scm:git:https://github.com/UdaraWanasinghe/android-extensions.git")
    scmDevConnection.set("scm:git:ssh://git@github.com/UdaraWanasinghe/android-extensions.git")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.android.material)
    implementation(libs.androidx.room.runtime)
    kapt(libs.androidx.room.compiler)
    implementation(libs.okhttp)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.aureusapps.providerfile)
    implementation(libs.androidx.test.espresso.core)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.test.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.room.runtime)
    kaptAndroidTest(libs.androidx.room.compiler)
    androidTestImplementation(libs.okhttp)
    androidTestImplementation(libs.okhttp.mockwebserver)
    androidTestImplementation(libs.androidx.test.core.ktx)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}