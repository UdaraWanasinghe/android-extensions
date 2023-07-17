// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.com.android.application) apply false
    alias(libs.plugins.com.android.library) apply false
    alias(libs.plugins.org.jetbrains.kotlin.android) apply false
    alias(libs.plugins.io.github.gradle.nexus.publish.plugin) apply true
}

project.extra.apply {
    set("version_code", 3)
    set("version_name", "1.0.2")
}

apply(from = "${rootDir}/scripts/publish-root.gradle")
apply(from = "${rootDir}/scripts/update-version.gradle")