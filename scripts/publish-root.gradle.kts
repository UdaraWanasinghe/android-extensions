import java.io.FileInputStream
import java.util.Properties

// Create variables with empty default values
project.extra["signing.keyId"] = ""
project.extra["signing.password"] = ""
project.extra["signing.key"] = ""
project.extra["ossrhUsername"] = ""
project.extra["ossrhPassword"] = ""
project.extra["sonatypeStagingProfileId"] = ""

val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    // Read local.properties file first if it exists
    val properties = Properties()
    FileInputStream(localPropertiesFile).use { input -> properties.load(input) }
    properties.forEach { name, value -> project.extra[name as String] = value }
} else {
    // Use system environment variables
    project.extra["ossrhUsername"] = System.getenv("OSSRH_USERNAME")
    project.extra["ossrhPassword"] = System.getenv("OSSRH_PASSWORD")
    project.extra["sonatypeStagingProfileId"] = System.getenv("SONATYPE_STAGING_PROFILE_ID")
    project.extra["signing.keyId"] = System.getenv("SIGNING_KEY_ID")
    project.extra["signing.password"] = System.getenv("SIGNING_PASSWORD")
    project.extra["signing.key"] = System.getenv("SIGNING_KEY")
}