abstract class UpdateVersionTask : DefaultTask() {

    @get:Option(option = "code", description = "The version code to set.")
    @get:Input
    abstract val versionCode: String

    @get:Option(option = "name", description = "The version name to set.")
    @get:Input
    abstract val versionName: String

    @TaskAction
    fun updateVersion() {
        updateReadme()
        updateRootProjectBuildGradle()
    }

    private fun updateReadme() {
        val file = project.file("README.md")
        if (file.exists()) {
            var text = file.readText()
            val pattern = Regex("\\d+.\\d+.\\d+")
            text = pattern.replace(text, versionName)
            file.writeText(text)
        }
    }

    private fun updateRootProjectBuildGradle() {
        val file = project.file("build.gradle.kts")
        if (file.exists()) {
            var text = file.readText()
            var pattern = Regex("[\"VERSION_CODE\"] = \\d+")
            text = pattern.replace(text, "[\"VERSION_CODE\"] = $versionCode")
            pattern = Regex("[\"VERSION_NAME\"] = \"\\d+.\\d+.\\d+\"")
            text = pattern.replace(text, "[\"VERSION_NAME\"] = \"$versionName\"")
            file.writeText(text)
        }
    }

}

tasks.register<UpdateVersionTask>("updateVersion") {
    group = "Version Control"
    description =
        "Updates the version code and name in the root project build.gradle.kts file and README.md file."
}