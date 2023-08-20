@file:Suppress("UnstableApiUsage")

import com.aureusapps.gradle.PublishLibraryConstants.GROUP_ID

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.aureusapps.gradle.update-version")
    id("com.aureusapps.gradle.publish-library")
    kotlin("kapt")
}

class Props(project: Project) {
    val groupId = project.findProperty(GROUP_ID).toString()
    val artifactId = "extensions"
    val versionName = "1.0.3"
}

val props = Props(project)

android {
    namespace = "${props.groupId}.${props.artifactId}"
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
    groupId.set(props.groupId)
    artifactId.set(props.artifactId)
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

    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")
    androidTestImplementation(libs.junit.ext)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.room.runtime)
    kaptAndroidTest(libs.room.compiler)
    androidTestImplementation(libs.okhttp)
    androidTestImplementation(libs.okhttp.mockwebserver)
    androidTestImplementation(libs.test.core.ktx)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}