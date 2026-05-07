plugins {
    idea
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.9"
}

val packBuildNumberFile = file("packBuildNumber.txt")
var packBuildNumber = if (packBuildNumberFile.exists()) packBuildNumberFile.readText().toInt() else 0
packBuildNumber++
packBuildNumberFile.writeText(packBuildNumber.toString())


// Reset all subproject build numbers
subprojects.forEach { subproject ->
    val buildNumberFile = subproject.file("buildNumber.txt")
    if (buildNumberFile.exists()) {
        buildNumberFile.writeText("-1")
    }
}

subprojects {
    apply(plugin = "org.jetbrains.gradle.plugin.idea-ext")
}