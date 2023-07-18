abstract class UpdateVersionTask : DefaultTask() {

    private var code: String = ""
    private var name: String = ""

    @Option(option = "code", description = "The version code to set.")
    fun setVersionCode(code: String) {
        this.code = code
    }

    @Option(option = "name", description = "The version name to set.")
    fun setVersionName(name: String) {
        this.name = name
    }

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
            text = pattern.replace(text, name)
            file.writeText(text)
        }
    }

    private fun updateRootProjectBuildGradle() {
        val file = project.file("build.gradle.kts")
        if (file.exists()) {
            var text = file.readText()
            var pattern = Regex("\\[\"VERSION_CODE\"] = \\d+")
            text = pattern.replace(text, "[\"VERSION_CODE\"] = $code")
            pattern = Regex("\\[\"VERSION_NAME\"] = \"\\d+.\\d+.\\d+\"")
            text = pattern.replace(text, "\\[\"VERSION_NAME\"] = \"$name\"")
            file.writeText(text)
        }
    }

}

tasks.register<UpdateVersionTask>("updateVersion") {
    group = "Version Control"
    description =
        "Updates the version code and name in the root project build.gradle.kts file and README.md file."
}