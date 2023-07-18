extensions.configure<PublishingExtension> {
    publications {
        afterEvaluate {
            register<MavenPublication>("release") {
                // standard metadata
                groupId = project.extra["PUBLISH_GROUP_ID"] as String
                artifactId = project.extra["PUBLISH_ARTIFACT_ID"] as String
                version = project.extra["PUBLISH_VERSION"] as String

                // publication component defined by the android library plugin
                from(components["release"])

                // pom file
                pom {
                    name.set(project.extra["PUBLISH_ARTIFACT_ID"] as String)
                    description.set("Useful extension functions and utility classes for android development.")
                    url.set("https://github.com/UdaraWanasinghe/android-extensions")
                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://github.com/UdaraWanasinghe/android-extensions/blob/main/LICENSE")
                        }
                    }
                    developers {
                        developer {
                            id.set("UdaraWanasinghe")
                            name.set("Udara Wanasinghe")
                            email.set("udara.developer@gmail.com")
                        }
                    }
                    scm {
                        connection.set("scm:git:github.com/UdaraWanasinghe/android-extensions.git")
                        developerConnection.set("scm:git:ssh://github.com/UdaraWanasinghe/android-extensions.git")
                        url.set("https://github.com/UdaraWanasinghe/android-extensions")
                    }
                }
            }
        }
    }
}

extensions.configure<SigningExtension> {
    useInMemoryPgpKeys(
        rootProject.extra["signing.keyId"] as String,
        rootProject.extra["signing.key"] as String,
        rootProject.extra["signing.password"] as String
    )
    val publishing = extensions.getByType<PublishingExtension>()
    sign(publishing.publications)
}