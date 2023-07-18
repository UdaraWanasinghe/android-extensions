import io.github.gradlenexus.publishplugin.NexusPublishExtension
import io.github.gradlenexus.publishplugin.NexusRepositoryContainer

plugins {
    alias(libs.plugins.com.android.application) apply false
    alias(libs.plugins.com.android.library) apply false
    alias(libs.plugins.org.jetbrains.kotlin.android) apply false
    alias(libs.plugins.io.github.gradle.nexus.publish.plugin) apply true
}

project.extra["VERSION_CODE"] = 3
project.extra["VERSION_NAME"] = "1.0.2"

apply(from = "$rootDir/scripts/publish-root.gradle.kts")
apply(from = "$rootDir/scripts/update-version.gradle.kts")

extensions.configure<NexusPublishExtension> {
    repositories(
        Action<NexusRepositoryContainer> {
            sonatype {
                nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
                stagingProfileId.set(project.extra["sonatypeStagingProfileId"] as String)
                username.set(project.extra["ossrhUsername"] as String)
                password.set(project.extra["ossrhPassword"] as String)
                snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            }
        }
    )
}